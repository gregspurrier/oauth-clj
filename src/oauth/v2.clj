(ns oauth.v2
  (:require [clj-http.core :as core])
  (:use [clj-http.client :only (request wrap-request)]
        [clojure.java.browse :only (browse-url)]
        oauth.util))

(defn- update-access-token [request access-token]
  (assoc-in request [:query-params "access_token"] access-token))

(defn oauth-access-token
  "Obtain the OAuth access token."
  [url client-id client-secret code redirect-uri]
  (-> {:method :get :url url
       :query-params
       {"client_id" client-id
        "client_secret" client-secret
        "code" code
        "redirect_uri" redirect-uri}}
      request :body parse-body))

(defn oauth-authorization-url
  "Returns the OAuth authorization url."
  [url client-id redirect-uri & {:as options}]
  (->> (assoc options :client-id client-id :redirect-uri redirect-uri)
       (format-query-params)
       (str url "?")))

(defn oauth-authorize
  "Send the user to the authorization url via `browse-url`."
  [url client-id redirect-uri & options]
  (browse-url (apply oauth-authorization-url url client-id redirect-uri options)))

(defn wrap-oauth-access-token
  "Returns a HTTP client that adds the OAuth `access-token` to `request`."
  [client & [access-token]]
  (fn [{:keys [oauth-access-token] :as request}]
    (client (update-access-token request (or oauth-access-token access-token)))))

(defn oauth-client
  "Returns a HTTP client for version 2 of the OAuth protocol."
  [access-token]
  (-> core/request
      (wrap-request)
      (wrap-oauth-access-token access-token)
      (wrap-decode-response)))
