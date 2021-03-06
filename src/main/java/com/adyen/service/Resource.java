/*
 *                       ######
 *                       ######
 * ############    ####( ######  #####. ######  ############   ############
 * #############  #####( ######  #####. ######  #############  #############
 *        ######  #####( ######  #####. ######  #####  ######  #####  ######
 * ###### ######  #####( ######  #####. ######  #####  #####   #####  ######
 * ###### ######  #####( ######  #####. ######  #####          #####  ######
 * #############  #############  #############  #############  #####  ######
 *  ############   ############  #############   ############  #####  ######
 *                                      ######
 *                               #############
 *                               ############
 *
 * Adyen Java API Library
 *
 * Copyright (c) 2018 Adyen B.V.
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more info.
 */
package com.adyen.service;

import java.io.IOException;
import java.util.List;
import com.adyen.Config;
import com.adyen.Service;
import com.adyen.httpclient.ClientInterface;
import com.adyen.httpclient.HTTPClientException;
import com.adyen.model.ApiError;
import com.adyen.service.exception.ApiException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Resource {

    protected static final Gson GSON = new Gson();
    protected String endpoint;
    protected List<String> requiredFields;
    private Service service;

    public Resource(Service service, String endpoint, List<String> requiredFields) {
        this.service = service;
        this.endpoint = endpoint;
        this.requiredFields = requiredFields;
    }

    /**
     * Request using json String
     */
    public String request(String json) throws ApiException, IOException {
        ClientInterface clientInterface = (ClientInterface) this.service.getClient().getHttpClient();
        Config config = this.service.getClient().getConfig();
        String responseBody;
        ApiException apiException;

        try {
            return clientInterface.request(this.endpoint, json, config, this.service.isApiKeyRequired());
        } catch (HTTPClientException e) {
            responseBody = e.getResponseBody();
            apiException = new ApiException(e.getMessage(), e.getCode());
        }

        // Enhance ApiException with more info from JSON payload
        try {
            ApiError apiError = GSON.fromJson(responseBody, new TypeToken<ApiError>() {
            }.getType());
            apiException.setError(apiError);
        } catch (JsonSyntaxException ignored) {
            throw new ApiException("Invalid response or an invalid X-API-Key key was used", apiException.getStatusCode());
        }

        throw apiException;
    }
}
