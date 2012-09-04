/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jclouds.imagestore.imagehoster.flickr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.RequestContext;
import com.googlecode.flickrjandroid.auth.Permission;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

/**
 * This class offers the OAuth authentication for Flickr.
 * 
 * @author Wolfgang Miller
 */
public class FlickrOAuth {
    /** The Flickr instance. */
    private final Flickr fl;
    /** The OAuth token. */
    private OAuthToken rToken;
    /** The access token. */
    private OAuth aToken;
    /** The verifier. */
    private String verifier;
    /** The Flickr properties. */
    private final Properties fp = new Properties();

    /**
     * Constructs Flickr OAuth authentication.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred
     */
    public FlickrOAuth() throws IOException {
        loadFlickrProperties();
        fl = new Flickr(FlickrOAuthConstants.FLICKR_APP_KEY, FlickrOAuthConstants.FLICKR_SHARED_SECRET);
    }

    /**
     * Authenticates the application and returns a authenticated Flickr instance.
     * 
     * @return a authenticated Flickr instance
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FlickrException
     *             Signals that a Flickr exception has occurred.
     */
    public Flickr getAuthenticatedFlickrInstance() throws IOException, FlickrException {

        if (fp.getProperty("token").isEmpty()) {
            rToken = fl.getOAuthInterface().getRequestToken(FlickrOAuthConstants.CALLBACK_URL);
            System.out.println(generateAuthenticationURL());
            readInVerifier();

            aToken = getAccessToken();
            saveOAuthToPropertiesFile();
        } else {
            aToken = loadOAuthFromPropertiesFile();
        }
        return fl;
    }

    /**
     * Loads Flickr properties from Flickr properties file.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void loadFlickrProperties() throws IOException {
        File propFile = new File(FlickrOAuthConstants.FLICKR_PROP_FILE_URI);
        
        if (!propFile.exists()) {
            propFile.createNewFile();
        }

        fp.load(new FileInputStream(propFile));
    }

    /**
     * Load authentication data from Flickr properties file.
     * 
     * @return the OAuth authentication.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private OAuth loadOAuthFromPropertiesFile() throws IOException {

        final String token = fp.getProperty(FlickrOAuthConstants.PROPKEY_TOKEN);
        final String tokenSecret = fp.getProperty(FlickrOAuthConstants.PROPKEY_TOKEN_SECRET);
        final String userId = fp.getProperty(FlickrOAuthConstants.PROPKEY_USER_ID);
        final String username = fp.getProperty(FlickrOAuthConstants.PROPKEY_USERNAME);

        User usr = new User();
        usr.setId(userId);
        usr.setUsername(username);
        OAuthToken tok = new OAuthToken(token, tokenSecret);

        OAuth oa = new OAuth();
        oa.setToken(tok);
        oa.setUser(usr);
        RequestContext.getRequestContext().setOAuth(oa);

        return oa;
    }

    /**
     * Saves authentication information in Flickr properties file.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void saveOAuthToPropertiesFile() throws IOException {

        final String token = aToken.getToken().getOauthToken();
        final String tokenSecret = aToken.getToken().getOauthTokenSecret();
        final String userId = aToken.getUser().getId();
        final String username = aToken.getUser().getUsername();

        fp.setProperty(FlickrOAuthConstants.PROPKEY_TOKEN, token);
        fp.setProperty(FlickrOAuthConstants.PROPKEY_TOKEN_SECRET, tokenSecret);
        fp.setProperty(FlickrOAuthConstants.PROPKEY_USER_ID, userId);
        fp.setProperty(FlickrOAuthConstants.PROPKEY_USERNAME, username);
        fp.store(new FileOutputStream(new File(FlickrOAuthConstants.FLICKR_PROP_FILE_URI)),
            FlickrOAuthConstants.FLICKR_PROP_FILE_NAME);
    }

    /**
     * Gets the user.
     * 
     * @return the user
     */
    public User getUser() {
        return aToken.getUser();
    }

    /**
     * Gets the token.
     * 
     * @return the token
     */
    OAuthToken getToken() {
        return aToken.getToken();
    }

    /**
     * Generate authentication URL.
     * 
     * @return the authentication URL.
     * @throws MalformedURLException
     *             Signals that malformed URL exception has occurred.
     */
    private URL generateAuthenticationURL() throws MalformedURLException {
        return fl.getOAuthInterface().buildAuthenticationUrl(FlickrOAuthConstants.FLICKR_PERMISSION, rToken);
    }

    /**
     * Read in verifier.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void readInVerifier() throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        System.out.println("Please enter your flickr authorization code:");
        verifier = br.readLine();
    }

    /**
     * Returns the access token.
     * 
     * @return the access token
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FlickrException
     *             the flickr exception
     */
    private OAuth getAccessToken() throws IOException, FlickrException {
        return fl.getOAuthInterface().getAccessToken(rToken.getOauthToken(), rToken.getOauthTokenSecret(),
            verifier);
    }

    private static class FlickrOAuthConstants {

        /** The callback-URL. oob = out-of-band. */
        private static final String CALLBACK_URL = "oob";
        /** The flickr permission. */
        private static final Permission FLICKR_PERMISSION = Permission.DELETE;
        /** The flickr properties file name. */
        private static final String FLICKR_PROP_FILE_NAME = "flickr.properties";
        /** The path to Flickr properties file. */
        private static final String FLICKR_PROP_FILE_URI = FlickrOAuth.class.getResource(".").getPath() + "../../../../../"  + FlickrOAuthConstants.FLICKR_PROP_FILE_NAME;

        // Application Identifier

        /** The application key. */
        private static final String FLICKR_APP_KEY = "3e6f5174edc3744e57c496db5d780ee8";
        /** The shared secret. */
        private static final String FLICKR_SHARED_SECRET = "a23933fe38c54919";

        // Properties Keys

        /** The token properties key. */
        private static final String PROPKEY_TOKEN = "token";
        /** The token secret properties key. */
        private static final String PROPKEY_TOKEN_SECRET = "tokenSecret";
        /** The user id properties key. */
        private static final String PROPKEY_USER_ID = "userId";
        /** The user name properties key. */
        private static final String PROPKEY_USERNAME = "usernmae";

    }
}