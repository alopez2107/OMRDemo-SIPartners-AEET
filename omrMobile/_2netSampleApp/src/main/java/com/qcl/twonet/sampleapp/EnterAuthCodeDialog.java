/**
 * Copyright (C) 2013-2014 Qualcomm Life, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of Qualcomm
 * Life, Inc.
 *
 * The following sample code illustrates various aspects of the 2net Mobile SDK.
 *
 * The sample code herein is provided for your convenience, and has not been
 * tested or designed to work on any particular system configuration. It is
 * provided AS IS and your use of this sample code, whether as provided or with
 * any modification, is at your own risk. Neither Qualcomm Life, Inc. nor any
 * affiliate takes any liability nor responsibility with respect to the sample
 * code, and disclaims all warranties, express and implied, including without
 * limitation warranties on merchantability, fitness for a specified purpose,
 * and against infringement.
 */

package com.qcl.twonet.sampleapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.qcl.twonet.sampleapp.R;

/**
 * Dialog to prompt the user to enter the required Auth Code
 */
public class EnterAuthCodeDialog extends DialogFragment {

    public interface EnterAuthCodeDialogListener {
        void onEnterAuthCodeDialogOk(String user, String pass);
        void onEnterAuthCodeDialogCancel();
	}

	Dialog mDialog;
    EnterAuthCodeDialogListener mListener;
    EditText mInputNameValue;
	EditText mInputCodeValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.enter_auth_code_dialog, null);

        mInputNameValue = (EditText) v.findViewById(R.id.input_user);
        //mInputNameValue.setText("bgoodman");
        mInputNameValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String name = mInputNameValue.getText().toString();
                    String pass = mInputCodeValue.getText().toString();
                    mListener.onEnterAuthCodeDialogOk(name, pass);
                    mDialog.dismiss();
                }
                return false;
            }
        });

        mInputCodeValue = (EditText) v.findViewById(R.id.input_pwd);
        //mInputCodeValue.setText(ParentAppActivity.wearpass); //rj? perhaps the pwd was spoken into the app
        mInputCodeValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String name = mInputNameValue.getText().toString();
                    String pass = mInputCodeValue.getText().toString();
                    mListener.onEnterAuthCodeDialogOk(name, pass);
                    mDialog.dismiss();
                }
                return false;
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.enter_auth_code).setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = mInputNameValue.getText().toString();
                        String pass = mInputCodeValue.getText().toString();
                        mListener.onEnterAuthCodeDialogOk(name, pass);
                    }
                }).setNegativeButton(android.R.string.cancel, mNegativeListener).setOnCancelListener(mCancelListener)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int code, KeyEvent event) {
                        if (code == KeyEvent.KEYCODE_BACK) {
                            mListener.onEnterAuthCodeDialogCancel();
                        }
                        return false;
                    }
                });

        return mDialog = builder.create();
	}

    final DialogInterface.OnClickListener mNegativeListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface di, int which) {
            mListener.onEnterAuthCodeDialogCancel();
        }
    };

    final DialogInterface.OnCancelListener mCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface di) {
            mListener.onEnterAuthCodeDialogCancel();
        }
    };

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
            mListener = (EnterAuthCodeDialogListener) activity;
		} catch (ClassCastException e) {
            throw new ClassCastException("Calling activity must implement EnterAuthCodeDialogListener");
		}
	}

}
