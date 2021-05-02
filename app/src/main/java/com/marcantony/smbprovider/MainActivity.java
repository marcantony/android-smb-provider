package com.marcantony.smbprovider;

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

import com.marcantony.smbprovider.data.ServerInfo;
import com.marcantony.smbprovider.data.ServerInfoRepository;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AddServerDialogFragment.AddServerDialogListener {

    private ServerListViewModel serverListViewModel;
    private ServerInfoAdapter serverInfoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        serverInfoAdapter = new ServerInfoAdapter(Collections.emptyList());
        RecyclerView recyclerView = findViewById(R.id.serverList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(serverInfoAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);

        serverListViewModel = new ViewModelProvider(this, new ServerListViewModel.Factory(ServerInfoRepository.getInstance()))
                .get(ServerListViewModel.class);
        final Observer<List<ServerInfo>> serverInfoObserver = serverInfoList ->
                serverInfoAdapter.setServers(serverInfoList);
        serverListViewModel.getServers().observe(this, serverInfoObserver);
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
        AddServerDialogFragment dialog = new AddServerDialogFragment();
        dialog.show(getSupportFragmentManager(), "AddServerDialogFragment");
    }

    public void openFiles(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivity(intent);
    }

    @Override
    public void onSave(ServerInfo info) {
        serverListViewModel.addServer(info);
    }
}