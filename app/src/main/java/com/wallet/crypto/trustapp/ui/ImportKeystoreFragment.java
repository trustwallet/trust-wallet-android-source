package com.wallet.crypto.trustapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.ui.widget.OnImportKeystoreListener;
import com.wallet.crypto.trustapp.util.FileUtils;


public class ImportKeystoreFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_CODE_FILE_SELECTOR = 101;

    private static final int REQUEST_CODE_PERMISSION_READ_FILE = 201;

    private static final String TAG = ImportKeystoreFragment.class.getSimpleName();

    private static final OnImportKeystoreListener dummyOnImportKeystoreListener = (k, p) -> {
    };

    private EditText keystore;
    private EditText password;
    private OnImportKeystoreListener onImportKeystoreListener;

    public static ImportKeystoreFragment create() {
        return new ImportKeystoreFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_import_keystore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        keystore = view.findViewById(R.id.keystore);
        password = view.findViewById(R.id.password);
        view.findViewById(R.id.btn_start_import_keystore).setOnClickListener(this);
        view.findViewById(R.id.btn_select_keystore).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_import_keystore:
                this.keystore.setError(null);
                String keystore = this.keystore.getText().toString();
                String password = this.password.getText().toString();
                if (TextUtils.isEmpty(keystore)) {
                    this.keystore.setError(getString(R.string.error_field_required));
                } else {
                    onImportKeystoreListener.onKeystore(keystore, password);
                }
                break;
            case R.id.btn_select_keystore:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSION_READ_FILE);
                        return;
                    } else {
                        performFileSelection();
                    }
                } else {
                    performFileSelection();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_FILE_SELECTOR:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        doGetSelectedKeystoreFileInfo(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.msg_failed_open_file_selector), Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_READ_FILE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performFileSelection();
            } else {
                // Permission Denied
                Toast.makeText(getActivity(), "Reading Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    public void setOnImportKeystoreListener(OnImportKeystoreListener onImportKeystoreListener) {
        this.onImportKeystoreListener = onImportKeystoreListener == null
                ? dummyOnImportKeystoreListener
                : onImportKeystoreListener;
    }

    private void performFileSelection() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.title_select_keystore)),
                    REQUEST_CODE_FILE_SELECTOR);
        } catch (Exception e) {
            Toast.makeText(getActivity(), getString(R.string.msg_failed_open_file_selector), Toast.LENGTH_SHORT).show();
        }

    }

    private void doGetSelectedKeystoreFileInfo(Intent data) {
        Uri uri = data.getData();
        Log.d(TAG, "File Uri: " + uri.toString());
        // Get the path
        if (uri != null) {
            TaskGetSelectedFileContent taskGetSelectedFileContent = new TaskGetSelectedFileContent(uri);
            taskGetSelectedFileContent.execute();
        }

    }

    private class TaskGetSelectedFileContent extends AsyncTask<Void, Void, String> {

        Uri fileUri;

        TaskGetSelectedFileContent(Uri uri) {
            fileUri = uri;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return FileUtils.readTextFileFromUri(getContext(), fileUri);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            keystore.setText(s);
        }
    }
}
