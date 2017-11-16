package ru.netradar.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
@ConfigurationProperties("acceptor")
public class AcceptorProperties {

    @NotNull
    private Integer portMapnav;

    @NotNull
    private Integer portTr102;

    @NotNull
    private Integer portArtal;

    @NotNull
    private Integer portDiag;
    @NotNull
    private Integer portMapnavUdp;
    @NotNull
    private Integer portMapnavUdpCount;

    @NotNull
    private Integer portViewer;

    @NotNull
    private Integer portAsyncTr102;

    public Integer getPortAsyncTr102() {
        return portAsyncTr102;
    }

    public void setPortAsyncTr102(Integer portAsyncTr102) {
        this.portAsyncTr102 = portAsyncTr102;
    }

    public Integer getPortMapnav() {
        return portMapnav;
    }

    public void setPortMapnav(Integer portMapnav) {
        this.portMapnav = portMapnav;
    }

    public Integer getPortTr102() {
        return portTr102;
    }

    public void setPortTr102(Integer portTr102) {
        this.portTr102 = portTr102;
    }

    public Integer getPortArtal() {
        return portArtal;
    }

    public void setPortArtal(Integer portArtal) {
        this.portArtal = portArtal;
    }

    public Integer getPortDiag() {
        return portDiag;
    }

    public void setPortDiag(Integer portDiag) {
        this.portDiag = portDiag;
    }

    public Integer getPortMapnavUdp() {
        return portMapnavUdp;
    }

    public void setPortMapnavUdp(Integer portMapnavUdp) {
        this.portMapnavUdp = portMapnavUdp;
    }

    public Integer getPortMapnavUdpCount() {
        return portMapnavUdpCount;
    }

    public void setPortMapnavUdpCount(Integer portMapnavUdpCount) {
        this.portMapnavUdpCount = portMapnavUdpCount;
    }

    public Integer getPortViewer() {
        return portViewer;
    }

    public void setPortViewer(Integer portViewer) {
        this.portViewer = portViewer;
    }
}
