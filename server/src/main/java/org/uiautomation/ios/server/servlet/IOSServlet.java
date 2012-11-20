/*
 * Copyright 2012 ios-driver committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.uiautomation.ios.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.testng.Reporter;
import org.uiautomation.ios.UIAModels.configuration.WorkingMode;
import org.uiautomation.ios.communication.FailedWebDriverLikeResponse;
import org.uiautomation.ios.communication.WebDriverLikeCommand;
import org.uiautomation.ios.communication.WebDriverLikeRequest;
import org.uiautomation.ios.communication.WebDriverLikeResponse;
import org.uiautomation.ios.server.CommandMapping;
import org.uiautomation.ios.server.ServerSideSession;
import org.uiautomation.ios.server.command.Handler;

public class IOSServlet extends DriverBasedServlet {

  private static final long serialVersionUID = -1190162363756488569L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      process(request, response);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      process(request, response);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {
    try {
      process(request, response);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void process(HttpServletRequest request, HttpServletResponse response) throws Exception {

    WebDriverLikeRequest req = new WebDriverLikeRequest(request);

    response.setContentType("application/json;charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(200);
    WebDriverLikeResponse resp;

    boolean nativeMode = true;
    try {
      ServerSideSession session = getDriver().getSession(req.getSession());
      nativeMode = session.getMode() == WorkingMode.Native;
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    String mode = nativeMode ? "native" : "web";
    // System.out.println(mode+"\t"+req.getMethod()+"\t"+req.getPath());

    resp = getResponse(req);

    // TODO implement the json protocol properly.
    if (req.getGenericCommand() == WebDriverLikeCommand.NEW_SESSION) {
      response.setStatus(301);
      String session = resp.getSessionId();

      String scheme = request.getScheme(); // http
      String serverName = request.getServerName(); // hostname.com
      int serverPort = request.getServerPort(); // 80
      String contextPath = request.getContextPath(); // /mywebapp

      // Reconstruct original requesting URL
      String url = scheme + "://" + serverName + ":" + serverPort + contextPath;
      response.setHeader("location", url + "/session/" + session);
    }

    response.getWriter().print(resp.stringify());
    response.getWriter().close();

  }

  private WebDriverLikeResponse getResponse(WebDriverLikeRequest request) {
    long start = System.currentTimeMillis();
    String command = "";
    try {
      WebDriverLikeCommand wdlc = request.getGenericCommand();
      Handler h = CommandMapping.get(wdlc).createHandler(getDriver(), request);
      command = wdlc.method() + "\t " + wdlc.path();
      return h.handleAndRunDecorators();
    } catch (Exception e) {
      try {
        return new FailedWebDriverLikeResponse(request.getSession(), e);
      } catch (Exception e1) {
        return new FailedWebDriverLikeResponse(null, e);
      }
    } finally {
      String message = (System.currentTimeMillis() - start) + "ms.\t"+command;
      System.out.println(message);
      Reporter.log(message);
    }

  }
}
