package com.marcantony.smbprovider;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class AddServerDialogFragment extends DialogFragment {

    public interface AddServerDialogListener {
        void onSave(ServerInfo info);
    }

    private AddServerDialogListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_server_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (AddServerDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AddServerDialogListener");
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
            listener.onSave(info);
            this.dismiss();
        } else {
            Snackbar.make(view, "Host must be provided", Snackbar.LENGTH_LONG).show();
        }
    }

    private void onClickCancel() {
        this.dismiss();
    }
}