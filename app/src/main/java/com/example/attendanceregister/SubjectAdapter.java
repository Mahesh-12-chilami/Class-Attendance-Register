package com.example.attendanceregister;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for managing subjects in SubjectManagementActivity.
 * Supports Edit and Delete callbacks.
 */
public class SubjectAdapter extends BaseAdapter {

    public interface OnSubjectActionListener {
        void onEdit(Subject subject);
        void onDelete(Subject subject);
    }

    private final Context context;
    private final List<Subject> subjectList;
    private final LayoutInflater inflater;
    private final OnSubjectActionListener listener;

    public SubjectAdapter(Context context, List<Subject> subjectList, OnSubjectActionListener listener) {
        this.context = context;
        this.subjectList = subjectList;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return subjectList != null ? subjectList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return subjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return subjectList.get(position).getId();
    }

    static class ViewHolder {
        TextView tvSemesterBadge;
        TextView tvSubjectName;
        ImageButton btnEdit;
        ImageButton btnDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_subject_manage, parent, false);
            holder = new ViewHolder();
            holder.tvSemesterBadge = convertView.findViewById(R.id.tvSubjectSemesterBadge);
            holder.tvSubjectName = convertView.findViewById(R.id.tvSubjectName);
            holder.btnEdit = convertView.findViewById(R.id.btnEditSubject);
            holder.btnDelete = convertView.findViewById(R.id.btnDeleteSubject);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Subject subject = subjectList.get(position);
        holder.tvSemesterBadge.setText(subject.getSemester());
        holder.tvSubjectName.setText(subject.getName());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(subject);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(subject);
        });

        return convertView;
    }
}
