package com.marcantony.smbprovider;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.marcantony.smbprovider.data.ServerInfo;

public class ServerInfoDialogFragment extends DialogFragment {

    public static final String ARG_INITIAL_HOST = "initial_host";
    public static final String ARG_INITIAL_SHARE = "initial_share";
    public static final String ARG_INITIAL_USERNAME = "initial_username";
    public static final String ARG_INITIAL_PASSWORD = "initial_password";
    public static final String ARG_INITIAL_ID = "initial_id";

    public interface SubmitListener {
        void onSubmit(ServerInfo info);
        void onDelete(ServerInfo info);
    }

    private SubmitListener listener = null;
    private ServerInfo initialState = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_server_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            initialState = new ServerInfo(
                    arguments.getString(ARG_INITIAL_HOST),
                    arguments.getString(ARG_INITIAL_SHARE),
                    arguments.getString(ARG_INITIAL_USERNAME),
                    arguments.getString(ARG_INITIAL_PASSWORD)
            );
            initialState.id = arguments.getInt(ARG_INITIAL_ID, ServerInfo.ID_UNSET);

            Button buttonDelete = view.findViewById(R.id.buttonDelete);
            buttonDelete.setVisibility(View.VISIBLE);
            buttonDelete.setOnClickListener(v -> onClickDelete());
        }

        Button buttonSave = view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> onClickSave(view));

        view.findViewById(R.id.buttonCancel).setOnClickListener(v -> onClickCancel());

        EditText editTextHostname = view.findViewById(R.id.editTextHostname);
        editTextHostname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                buttonSave.setEnabled(s.length() > 0);
            }
        });

        if (initialState != null) {
            editTextHostname.setText(initialState.host);

            EditText share = view.findViewById(R.id.editTextShare);
            share.setText(initialState.share);

            EditText username = view.findViewById(R.id.editTextUsername);
            username.setText(initialState.username);

            EditText password = view.findViewById(R.id.editTextPassword);
            password.setText(initialState.password);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (SubmitListener) context;
        } catch (ClassCastException e) {
            Log.i("ServerInfoDialog", "host activity does not implement SubmitListener");
        }
    }

    private void onClickSave(View view) {
        EditText hostname = view.findViewById(R.id.editTextHostname);
        EditText share = view.findViewById(R.id.editTextShare);
        EditText username = view.findViewById(R.id.editTextUsername);
        EditText password = view.findViewById(R.id.editTextPassword);

        if (hostname.length() > 0) {
            ServerInfo info = new ServerInfo(
                    hostname.getText().toString(),
                    share.getText().toString(),
                    username.getText().toString(),
                    password.getText().toString()
            );

            if (initialState != null) {
                info.id = initialState.id;
            }

            listener.onSubmit(info);
            this.dismiss();
        } else {
            Snackbar.make(view, "Host must be provided", Snackbar.LENGTH_LONG).show();
        }
    }

    private void onClickCancel() {
        this.dismiss();
    }

    private void onClickDelete() {
        listener.onDelete(initialState);
        this.dismiss();
    }
}