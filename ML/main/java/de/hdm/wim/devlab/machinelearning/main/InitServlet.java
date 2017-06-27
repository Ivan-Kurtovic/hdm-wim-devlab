/*
 * Copyright (c) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package de.hdm.wim.devlab.machinelearning.main;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PushConfig;
import com.google.api.services.pubsub.model.Subscription;
import com.google.api.services.pubsub.model.Topic;

import de.hdm.wim.devlab.machinelearning.util.PubsubUtils;

/**
 * 
 * Entry point der die benötigten PubSub-ressourcen bereitstellt.
 * 
 */
@SuppressWarnings("serial")
public class InitServlet extends HttpServlet {

    @Override
    public final void doGet(final HttpServletRequest req,
                            final HttpServletResponse resp)
            throws IOException {
    	
    	
        Pubsub client = PubsubUtils.getClient();
        String projectId = PubsubUtils.getProjectId();
        req.setAttribute("project", projectId);

        setupTopic(client);
        setupSubscription(client);

        req.setAttribute("topic", PubsubUtils.getAppTopicName());
        req.setAttribute("subscription", PubsubUtils.getAppSubscriptionName());
        req.setAttribute("subscriptionEndpoint",
                PubsubUtils.getAppEndpointUrl()
                        .replaceAll("token=[^&]*", "token=REDACTED"));
        RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/main.jsp");
        try {
            rd.forward(req, resp);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    /**
     * Erstellt ein Cloud Pub/Sub Topic wenn es noch nicht existiert.
     *
     * @param client Pubsub client object.
     * @throws IOException wenn der API-Aufruf zu  Cloud Pub/Sub fehlschlägt.
     */
    private void setupTopic(final Pubsub client) throws IOException {
        String fullName = String.format("projects/%s/topics/%s",
                PubsubUtils.getProjectId(),
                PubsubUtils.getAppTopicName());

        try {
            client.projects().topics().get(fullName).execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_NOT_FOUND) {
                // Create the topic if it doesn't exist
                client.projects().topics()
                        .create(fullName, new Topic())
                        .execute();
            } else {
                throw e;
            }
        }
    }

    /**
     * 
     * Erstellt ein Cloud Pub/Sub Subscription wenn diese noch nicht existiert.
     *
     * @param client Pubsub client object.
     * @throws IOException wenn der API-Aufruf zu  Cloud Pub/Sub fehlschlägt
     */
    private void setupSubscription(final Pubsub client) throws IOException {
        String fullName = String.format("projects/%s/subscriptions/%s",
                PubsubUtils.getProjectId(),
                PubsubUtils.getAppSubscriptionName());

        try {
            client.projects().subscriptions().get(fullName).execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_NOT_FOUND) {
                // Create the subscription if it doesn't exist
                String fullTopicName = String.format("projects/%s/topics/%s",
                        PubsubUtils.getProjectId(),
                        PubsubUtils.getAppTopicName());
                PushConfig pushConfig = new PushConfig()
                        .setPushEndpoint(PubsubUtils.getAppEndpointUrl());
                Subscription subscription = new Subscription()
                        .setTopic(fullTopicName)
                        .setPushConfig(pushConfig);
                client.projects().subscriptions()
                        .create(fullName, subscription)
                        .execute();
            } else {
                throw e;
            }
        }
    }
    
    
    
    
}
