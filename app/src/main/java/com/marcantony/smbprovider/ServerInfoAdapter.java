package com.marcantony.smbprovider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marcantony.smbprovider.domain.ServerInfo;

import java.util.List;

public class ServerInfoAdapter extends RecyclerView.Adapter<ServerInfoAdapter.ViewHolder> {

    public interface ServerStatusListener {
        void onServerEdited(ServerInfo info);
    }

    private List<ServerInfo> servers;
    private final ServerStatusListener listener;
    private final FragmentManager manager;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView root;
        private final TextView user;
        private final Switch switchEnabled;
        private ServerInfo info;

        public ViewHolder(View view, ServerStatusListener listener, FragmentManager manager) {
            super(view);

            view.setOnClickListener(v -> {
                DialogFragment dialog = new ServerInfoDialogFragment();
                Bundle args = new Bundle();
                args.putString(ServerInfoDialogFragment.ARG_INITIAL_HOST, info.host);
                args.putString(ServerInfoDialogFragment.ARG_INITIAL_SHARE, info.share);
                args.putString(ServerInfoDialogFragment.ARG_INITIAL_USERNAME, info.username);
                args.putString(ServerInfoDialogFragment.ARG_INITIAL_PASSWORD, info.password);
                args.putInt(ServerInfoDialogFragment.ARG_INITIAL_ID, info.id);
                dialog.setArguments(args);
                dialog.show(manager, "server info list");
            });

            root = view.findViewById(R.id.textViewRoot);
            user = view.findViewById(R.id.textViewUser);
            switchEnabled = view.findViewById(R.id.switchServerEnabled);

            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (info != null) {
                    info.setEnabled(isChecked);
                    listener.onServerEdited(info);
                }
            });
        }

        public void bindData(ServerInfo info) {
            String share = info.share == null ? "" : info.share;
            String userText = info.username == null ? "Anonymous" : info.username;

            root.setText(String.format("%s/%s", info.host, share));
            user.setText(userText);
            switchEnabled.setChecked(info.isEnabled());
            this.info = info;
        }

    }

    public ServerInfoAdapter(List<ServerInfo> servers, ServerStatusListener listener, FragmentManager manager) {
        this.servers = servers;
        this.listener = listener;
        this.manager = manager;
    }

    public void setServers(List<ServerInfo> servers) {
        this.servers = servers;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.server_row_item;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);
        return new ViewHolder(view, listener, manager);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServerInfo info = servers.get(position);
        holder.bindData(info);
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

}
