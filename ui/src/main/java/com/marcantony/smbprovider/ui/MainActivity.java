package com.marcantony.smbprovider.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marcantony.smbprovider.persistence.RoomServerInfoRepository;
import com.marcantony.smbprovider.domain.ServerInfo;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ServerInfoDialogFragment.SubmitListener {

    private ServerListViewModel serverListViewModel;
    private ServerInfoAdapter serverInfoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        serverListViewModel = new ViewModelProvider(this,
                new ServerListViewModel.Factory(RoomServerInfoRepository.getInstance(getApplicationContext())))
                .get(ServerListViewModel.class);
        final Observer<List<ServerInfo>> serverInfoObserver = serverInfoList ->
                serverInfoAdapter.setServers(serverInfoList);
        serverListViewModel.getServers().observe(this, serverInfoObserver);

        serverInfoAdapter = new ServerInfoAdapter(Collections.emptyList(), ((info) ->
                serverListViewModel.updateServer(info)), getSupportFragmentManager());
        RecyclerView recyclerView = findViewById(R.id.serverList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(serverInfoAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showDialog(View view) {
        ServerInfoDialogFragment dialog = new ServerInfoDialogFragment();
        dialog.show(getSupportFragmentManager(), "AddServerDialogFragment");
    }

    public void openFiles(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivity(intent);
    }

    @Override
    public void onSubmit(ServerInfo info) {
        serverListViewModel.addOrUpdateServer(info);
    }

    @Override
    public void onDelete(ServerInfo info) {
        serverListViewModel.deleteServer(info);
    }
}