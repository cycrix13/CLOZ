/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hienbibi.cloz;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.Settings;

public class LoginUsingActivityActivity extends Activity {
    private static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";

    private TextView textInstructionsOrLink;
    private Button buttonLoginLogout;
    Bitmap image;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
    private static final List<String> PERMISSIONS = Arrays.asList("publish_stream");
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
           if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
                //
            
        }else {
        	//Session.openActiveSession(this, true, statusCallback)
;        }
        
        image = BitmapFactory.decodeResource(getResources(), R.drawable.add);
        int w = image.getWidth();
        int h = image.getHeight();
    }

    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
        
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    

    private void onClickLogin() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(this, true, statusCallback);
        }
    }

    private void onClickLogout() {
        Session session = Session.getActiveSession();
        if (!session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }

    private void publishPhoto() {
    	 ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] byteArray = stream.toByteArray();

        Bundle params = new Bundle();

        params.putByteArray("picture", byteArray);
        Request request = new Request(Session.getActiveSession(), "me/photos",params, 
                HttpMethod.POST);
        request.setCallback(new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                if (response.getError()==null) {
                    Toast.makeText(LoginUsingActivityActivity.this, "Successfully posted photo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginUsingActivityActivity.this, response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        request.executeAsync();




    }
    
    
    private class SessionStatusCallback implements Session.StatusCallback {
    	
    	
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        	if (state == SessionState.OPENED) {
        		Session.getActiveSession().requestNewPublishPermissions(new NewPermissionsRequest(LoginUsingActivityActivity.this, PERMISSIONS).setCallback(new StatusCallback() {
					
					@Override
					public void call(Session session, SessionState state, Exception exception) {
						// TODO Auto-generated method stub
						if (state == SessionState.OPENED_TOKEN_UPDATED) {
			        		
				        	/*Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
				                @Override
				                public void onCompleted(Response response) {
				                    //showPublishResult(getString(R.string.photo_post), response.getGraphObject(), response.getError());
				                }
				            });
				            request.executeAsync();*/
							
							publishPhoto();
			        	}
					}
				}));
        	}
        	
        }
    }
}
