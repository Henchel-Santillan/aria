package com.example.aria.ui.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.aria.databinding.FragmentDialogNameRecordingBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class NameRecordingDialogFragment extends DialogFragment {

    FragmentDialogNameRecordingBinding binding;

    public interface NameRecordingDialogFragmentListener {
        void onNameRecordSave(String name);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDialogNameRecordingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText editText = binding.nameRecordingDialogFragmentInputEditText;
        TextInputLayout inputLayout = binding.nameRecordingDialogFragmentTextInputLayout;
        Button saveButton = binding.nameRecordingDialogFragmentSaveButton;
        saveButton.setEnabled(false);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) { /* No-op */ }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                if (charSequence.length() <= 0) {
                    inputLayout.setError("Empty name not allowed.");
                    inputLayout.setErrorEnabled(true);
                    editText.requestFocus();
                } else {
                    inputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null)
                    saveButton.setEnabled(editable.toString().length() != 0);
            }
        });

        saveButton.setOnClickListener((scopedView) -> {
            NameRecordingDialogFragmentListener listener = (NameRecordingDialogFragmentListener) getParentFragment();
            if (listener != null) {
                String recordingName = editText.getEditableText().toString() + ".mp4";
                listener.onNameRecordSave(recordingName);
            }
            dismiss();
        });

        binding.nameRecordingDialogFragmentCancelButton.setOnClickListener((scopedView) -> dismiss() );
    }

    public static String TAG = "NameRecordingDialogFragment";
}
