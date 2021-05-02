package com.marcantony.smbprovider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServerInfoAdapter extends RecyclerView.Adapter<ServerInfoAdapter.ViewHolder> {

    private List<ServerInfo> servers;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView root;
        private final TextView user;
        private final Switch switchEnabled;

        public ViewHolder(View view) {
            super(view);

            root = view.findViewById(R.id.textViewRoot);
            user = view.findViewById(R.id.textViewUser);
            switchEnabled = view.findViewById(R.id.switchServerEnabled);
        }

        public void bindData(ServerInfo info) {
            String share = info.share == null ? "" : info.share;
            String userText = info.username == null ? "Anonymous" : info.username;

            root.setText(String.format("%s/%s", info.host, share));
            user.setText(userText);
            switchEnabled.setChecked(true);
        }

    }

    public ServerInfoAdapter(List<ServerInfo> servers) {
        this.servers = servers;
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
        return new ViewHolder(view);
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
