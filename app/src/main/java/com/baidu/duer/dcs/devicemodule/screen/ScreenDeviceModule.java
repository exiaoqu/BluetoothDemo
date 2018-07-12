/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.duer.dcs.devicemodule.screen;

import com.baidu.duer.dcs.devicemodule.screen.message.HtmlPayload;
import com.baidu.duer.dcs.devicemodule.screen.message.LinkClickedPayload;
import com.baidu.duer.dcs.devicemodule.screen.message.RenderVoiceInputTextPayload;
import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException;
import com.baidu.duer.dcs.framework.BaseDeviceModule;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Event;
import com.baidu.duer.dcs.framework.message.Header;
import com.baidu.duer.dcs.framework.message.MessageIdHeader;
import com.baidu.duer.dcs.framework.message.Payload;
import com.baidu.duer.dcs.systeminterface.IWebView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Screen模块处理并执行服务下发的指令，如HtmlView指令，以及发送事件，如LinkClicked事件
 * <p>
 * Created by wuruisheng on 2017/5/31.
 */
public class ScreenDeviceModule extends BaseDeviceModule {
    private final IWebView webView;
    private List<IRenderVoiceInputTextListener> listeners;
    private final List<IRenderListener> renderListeners;

    public ScreenDeviceModule(IWebView webView, IMessageSender messageSender) {
        super(ApiConstants.NAMESPACE, messageSender);
        this.webView = webView;
        webView.addWebViewListener(new IWebView.IWebViewListener() {
            @Override
            public void onLinkClicked(String url) {
                sendLinkClickedEvent(url);
            }
        });
        this.listeners = Collections.synchronizedList(new ArrayList<IRenderVoiceInputTextListener>());
        renderListeners = new ArrayList<>();
    }

    @Override
    public ClientContext clientContext() {
        return null;
    }

    @Override
    public void handleDirective(Directive directive) throws HandleDirectiveException {
        String name = directive.header.getName();
        if (name.equals(ApiConstants.Directives.HtmlView.NAME)) {
            // 加载资源(url)页面
            handleHtmlPayload(directive.getPayload());
        } else if (name.equals(ApiConstants.Directives.RenderVoiceInputText.NAME)) {
            handleRenderVoiceInputTextPayload(directive.getPayload());
            handleScreenDirective(directive);
        } else if (name.equals(ApiConstants.Directives.RenderCard.NAME)) {
            handleScreenDirective(directive);
        } else if (name.equals(ApiConstants.Directives.RenderHint.NAME)) {
            handleScreenDirective(directive);
        } else {
            String message = "VoiceOutput cannot handle the directive";
            throw (new HandleDirectiveException(HandleDirectiveException.ExceptionType.UNSUPPORTED_OPERATION, message));
        }
    }

    @Override
    public void release() {
        listeners.clear();
    }

    private void handleHtmlPayload(Payload payload) {
        if (payload instanceof HtmlPayload) {
            HtmlPayload htmlPayload = (HtmlPayload) payload;
            webView.loadUrl(htmlPayload.getUrl());
        }
    }

    String html = "<html><head><meta charset=\"utf-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><script src=\"http://duer.bdstatic.com/saiya/dcsview/main.e239b3.js\"></script><style></style></head><body>\n" +
            "<div id=\"display\">\n" +
            "<section data-from=\"server\" class=\"head p-box-out\">" +
            "<div class=\"avatar\"></div>" +
            "<div class=\"bubble-container\">" +
            "<div class=\"bubble p-border text\">" +
            "<div class=\"text-content text\">%s</div></div></div>" +
            "</section>\n" +
            "</div></body></html>";

    String htmlFig = "<html>\n" +
            "  <head>\n" +
            "    <meta charset=\"utf-8\" />\n" +
            "    <title>图片插入html 在线演示</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <div class=\"scale\">\n" +
            "      <img src=\"http://47.94.250.178:8080/png/a.gif\" alt=\"\" /></p>\n" +
            "    </div>\n" +
            "    <p>%s</p>\n" +
            "  </body>\n" +
            "</html>";

    String htmlFig2 = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "  <head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "    <script src=\"http://duer.bdstatic.com/saiya/dcsview/main.e239b3.js\"></script>\n" +
            "    <style></style>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <div id=\"display\">\n" +
            "      <section data-from=\"server\" class=\"head p-box-out\">\n" +
            "        <div class=\"avatar\"></div>\n" +
            "        <div class=\"bubble-container\">\n" +
            "          <div class=\"bubble p-border text\">\n" +
            "            <div class=\"text-content text\">%s</div>\n" +
            "          </div>\n" +
            "        </div>\n" +
            "       <div class=\"scale\">\n" +
            "          <img style=\"width:100%;\" src=\"http://47.94.250.178:8080/png/a.png\" alt=\"\"/>\n" +
            "        </div>\n" +
            "      </section>\n" +
            "    </div>\n" +
            "  </body>\n" +
            "</html>";

    @Override
    public void handleInterestDirective(String interestedText) {
        if (interestedText.equals("")) {
            return;
        }
        webView.loadUrl("http://47.94.250.178:8080/html/imgVIDEO");
//        webView.loadData(String.format(htmlFig, interestedText), "text/html; charset=UTF-8", null);
    }

    private void handleRenderVoiceInputTextPayload(Payload payload) {
        RenderVoiceInputTextPayload textPayload = (RenderVoiceInputTextPayload) payload;
        fireRenderVoiceInputText(textPayload);
    }

    private void handleScreenDirective(Directive directive) {
        for (IRenderListener listener : renderListeners) {
            listener.onRenderDirective(directive);
        }
    }

    private void sendLinkClickedEvent(String url) {
        String name = ApiConstants.Events.LinkClicked.NAME;
        Header header = new MessageIdHeader(getNameSpace(), name);

        LinkClickedPayload linkClickedPayload = new LinkClickedPayload(url);
        Event event = new Event(header, linkClickedPayload);
        if (messageSender != null) {
            messageSender.sendEvent(event);
        }
    }

    private void fireRenderVoiceInputText(RenderVoiceInputTextPayload payload) {
        for (IRenderVoiceInputTextListener listener : listeners) {
            listener.onRenderVoiceInputText(payload);
        }
    }

    public void addRenderVoiceInputTextListener(IRenderVoiceInputTextListener listener) {
        listeners.add(listener);
    }

    public void addRenderListener(IRenderListener listener) {
        renderListeners.add(listener);
    }

    public void removeRenderListener(IRenderListener listener) {
        renderListeners.remove(listener);
    }

    public interface IRenderVoiceInputTextListener {
        /**
         * 接收到RenderVoiceInputText指令时回调
         *
         * @param payload 内容
         */
        void onRenderVoiceInputText(RenderVoiceInputTextPayload payload);

    }

    public interface IRenderListener {
        void onRenderDirective(Directive directive);
    }
}
