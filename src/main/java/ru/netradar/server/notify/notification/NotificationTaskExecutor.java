package ru.netradar.server.notify.notification;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.config.properties.DeleteMonitorProperties;
import ru.netradar.config.properties.NotificationQueueProperties;
import ru.netradar.server.PayService;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.notify.NotifyType;
import ru.netradar.server.device.notify.Recipient;
import ru.netradar.server.http.SiteClient;
import ru.netradar.server.notify.sms.SMSTaskExecutor;
import ru.netradar.server.queue.TaskExecutor;
import ru.netradar.server.queue.dao.TaskDAO;
import ru.netradar.server.storage.NotificationNotifier;
import ru.netradar.util.MD5;
import ru.netradar.utils.IdGenerator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Доставка положения
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:05:07
 * To change this template use File | Settings | File Templates.
 */
public class NotificationTaskExecutor implements TaskExecutor<NotificationTask>, NotificationNotifier {
    private static final Logger LOG = Logger.getLogger(NotificationTaskExecutor.class);
    private final String updateURI;
    private final String md5Key;
    private final IdGenerator idGenerator;
    private static final long MINUTE = 60000;
    private static final long DAY = 24 * 60 * MINUTE;
    private static final long WEEK = 7 * DAY;
    private final TransactionTemplate txTemplate;
    private final TaskDAO<NotificationTask> taskDAO;
    private final SMSTaskExecutor smsTaskExecutor;
    private final SiteClient siteClient;

    public NotificationTaskExecutor(NotificationQueueProperties notificationQueueProperties,
                                    DeleteMonitorProperties delSettings,
                                    TransactionTemplate txTemplate, TaskDAO<NotificationTask> positionTaskTaskDAO,
                                    final SMSTaskExecutor smsTaskExecutor,
                                    SiteClient siteClient,
                                    IdGenerator idGenerator) {
        this.txTemplate = txTemplate;
        this.taskDAO = positionTaskTaskDAO;
        this.smsTaskExecutor = smsTaskExecutor;
        this.siteClient = siteClient;
        this.updateURI = notificationQueueProperties.getUri();
        this.md5Key = delSettings.getRspass();
        this.idGenerator = idGenerator;
    }

    @Override
    public boolean processTask(NotificationTask task) {
        if (task.getExpiryDt().getTime() < System.currentTimeMillis()) {
            LOG.warn("Task expired " + task);
            return true;
        }

        if (task.getNotifyType() == NotifyType.NOTIFY_BY_SMS) {
            return processSMSNotification(task);
        }
        return true;
    }

    private boolean processSMSNotification(NotificationTask task) {
        NotificationPaymentStatus payed = checkoutUserAccountWithTask(task);
        if (payed == NotificationPaymentStatus.PAYED) {
            LOG.info("User " + task.getUserId() + " has payed for notification " + task);
            try {
                createSMSAlert(task);
            } catch (Throwable t) {
                LOG.error("SMS create task: " + t.getMessage(), t);
                return true;
            }
            return true;
        }
        if (payed == NotificationPaymentStatus.FAILED) {
            LOG.info("User " + task.getUserId() + " NOT payed for notification ");
            return true;
        }
        return false;
    }

    private void createSMSAlert(NotificationTask task) {
        smsTaskExecutor.createNotification(task.getRecipientAddress(), task.getInfo(), task.getId());
    }

    private NotificationPaymentStatus checkoutUserAccountWithTask(NotificationTask task) {

        final List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        /*$oper = $_GET['oper'];
$serviceName = $_GET['service_name'];
$extId = $_GET['ext_id'];
$userId = $_GET['user_id'];
$comment = $_GET['comment'];
$sign = $_GET['sign'];*/

        String oper = "RESERVE";
        String serviceName = PayService.SMS_NOTIFICATION.getName();
        String extId = "" + task.getId();
        String userId = "" + task.getUserId();
        String comment = task.getInfo();

        String s2sign = oper + serviceName + extId + userId + comment;
        final String sign = MD5.getHashString(s2sign + md5Key);

        params.add(new BasicNameValuePair("oper", oper));
        params.add(new BasicNameValuePair("service_name", serviceName));
        params.add(new BasicNameValuePair("ext_id", extId));
        params.add(new BasicNameValuePair("user_id", userId));
        params.add(new BasicNameValuePair("comment", comment));
        params.add(new BasicNameValuePair("sign", sign));

        HttpPost httpUriRequest = new HttpPost(updateURI);
        final UrlEncodedFormEntity encodedFormEntity;
        try {
            encodedFormEntity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.fatal(" on encode:" + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        httpUriRequest.setEntity(encodedFormEntity);
        HttpResponse httpResponse = null;
        String response;
        long startTime = System.currentTimeMillis();
        try {
            httpResponse = siteClient.executeHttpMethod(httpUriRequest);
            startTime = System.currentTimeMillis() - startTime;
            response = EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            LOG.error("Connect error: " + e.getMessage() + ", request: " + params);
            return null;
        } finally {
            if (httpResponse != null) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        LOG.info("Response (" + startTime + " ms): " + response);

        if (response.equals("PAYED")) return NotificationPaymentStatus.PAYED;
        if (response.startsWith("FAILED")) return NotificationPaymentStatus.FAILED;

        throw new IllegalArgumentException("Unexpected error: " + response + " for request " + params);

    }

    @Override
    public void createNotification(NRDevice device, Recipient recipient, String info) {
        final long time = System.currentTimeMillis();
        final NotificationTask task = new NotificationTask(idGenerator.generate(),
        new Timestamp(time), 0,
                new Timestamp(System.currentTimeMillis() + WEEK),
                device.userId, device.userType, info,
                new Timestamp(time), recipient.getRecipientAddress(),
                recipient.getNotifyType(), recipient.getUserId());

        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                taskDAO.storeTask(task);
            }
        });
        LOG.info("Notification for " + recipient.getUserId() + ": " + task.getInfo());
    }
}