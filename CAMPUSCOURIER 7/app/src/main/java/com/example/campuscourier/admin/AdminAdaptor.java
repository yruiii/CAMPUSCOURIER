package com.example.campuscourier.admin;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuscourier.R;
import com.example.campuscourier.shared.Requests_2;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


import java.util.ArrayList;

public class AdminAdaptor extends RecyclerView.Adapter<AdminAdaptor.ViewHolder> {
    private ArrayList<Requests_2> reportItems;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdminAdaptor(ArrayList<Requests_2> reportItems, Context context) {
        this.reportItems = reportItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_card_admin_report, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Requests_2 reportItem = reportItems.get(position);
        holder.bind(reportItem);
    }

    @Override
    public int getItemCount() {
        return reportItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView reportId, description, reasonDetail, otherReason, otherDescription;
        private EditText pointsDeducted;
        private Button buttonAccept, buttonDecline;
        private String docId, userId; // Document ID

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reportId = itemView.findViewById(R.id.telegram_handle);
            description = itemView.findViewById(R.id.Reason_detail);
            reasonDetail = itemView.findViewById(R.id.Reason_report);
            otherReason = itemView.findViewById(R.id.Other_detail);
            otherDescription = itemView.findViewById(R.id.Other_Description);
            buttonAccept = itemView.findViewById(R.id.buttonAccept);
            buttonDecline = itemView.findViewById(R.id.buttonDecline);
            pointsDeducted = itemView.findViewById(R.id.Points_deducted);

            pointsDeducted.setFilters(new InputFilter[]{new InputFilterMinMax(1, 100)});

            buttonAccept.setOnClickListener(v -> handleAcceptButtonClick());
            buttonDecline.setOnClickListener(v -> handleDeclineButtonClick());
        }

        public void bind(Requests_2 reportItem) {
            // Bind data to views
            reportId.setText(reportItem.getReportId());
            description.setText(reportItem.getDescription());
            reasonDetail.setText(reportItem.getSelectedDescription().toString());
            otherReason.setText(reportItem.getOtherReason());

            if (reportItem.getSelectedDescription().contains("Others")) {
                otherReason.setVisibility(View.VISIBLE);
                otherReason.setText(reportItem.getOtherReason());
                otherDescription.setVisibility(View.VISIBLE);
            } else {
                otherReason.setVisibility(View.GONE);
                otherDescription.setVisibility(View.GONE);
            }

            // Set the document ID
            docId = reportItem.getDocId();
            userId = reportItem.getUserId();
        }

        private void handleAcceptButtonClick() {
            String pointsText = pointsDeducted.getText().toString();
            if (pointsText.isEmpty()) {
                Toast.makeText(context, "Please fill up the points you want to deduct", Toast.LENGTH_SHORT).show();
            } else {
                int pointsToDeduct = Integer.parseInt(pointsText);
                updateReportsAndUsers(pointsToDeduct);
                removeItem(getAdapterPosition());
            }
        }

        private void handleDeclineButtonClick() {
            db.collection("report").document(docId).update("status", "Decline");
            db.collection("users").document(userId).collection("report").document(docId).update("status", "Declined");
            removeItem(getAdapterPosition());
        }

        private void updateReportsAndUsers(int pointsToDeduct) {
            db.collection("report").document(docId).update("status", "Accepted");
            db.collection("users").document(userId).collection("report").document(docId).update("status", "Accepted");

            db.collection("users")
                    .whereEqualTo("telegram", reportId.getText().toString().trim())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Object pointsObj = document.get("points");
                            if (pointsObj instanceof Long) {
                                long currentPoints = (Long) pointsObj;
                                int updatedPoints = (int) (currentPoints - pointsToDeduct);

                                // Update the points field in Firestore
                                db.collection("users").document(document.getId())
                                        .update("points", updatedPoints)
                                        .addOnSuccessListener(aVoid -> {
                                            if (updatedPoints < 0) {
                                                Toast.makeText(context, "Following user has messed up too many times, Please disable account", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, "Points deducted successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Failed to deduct points", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(context, "User points field not found or invalid type", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                    });
        }

        private void removeItem(int position) {
            reportItems.remove(position);
            notifyItemRemoved(position);
        }

        private class InputFilterMinMax implements InputFilter {
            private int min, max;

            public InputFilterMinMax(int min, int max) {
                this.min = min;
                this.max = max;
            }

            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    int input = Integer.parseInt(dest.toString() + source.toString());
                    if (isInRange(min, max, input)) {
                        return null;
                    }
                } catch (NumberFormatException ignored) {
                }
                return "";
            }

            private boolean isInRange(int a, int b, int c) {
                return b > a ? c >= a && c <= b : c >= b && c <= a;
            }
        }
    }
}
