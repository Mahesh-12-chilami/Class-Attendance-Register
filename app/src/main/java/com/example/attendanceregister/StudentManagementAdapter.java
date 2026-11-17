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
 * Adapter for managing students in StudentManagementActivity.
 * Supports Edit and Delete action callbacks.
 */
public class StudentManagementAdapter extends BaseAdapter {

    public interface OnStudentActionListener {
        void onEdit(Student student);
        void onDelete(Student student);
    }

    private final Context context;
    private final List<Student> studentList;
    private final LayoutInflater inflater;
    private final OnStudentActionListener listener;

    public StudentManagementAdapter(Context context, List<Student> studentList, OnStudentActionListener listener) {
        this.context = context;
        this.studentList = studentList;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return studentList != null ? studentList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return studentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return studentList.get(position).getId();
    }

    static class ViewHolder {
        TextView tvRegNo;
        TextView tvSection;
        TextView tvName;
        ImageButton btnEdit;
        ImageButton btnDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_student_manage, parent, false);
            holder = new ViewHolder();
            holder.tvRegNo = convertView.findViewById(R.id.tvStudentRegNo);
            holder.tvSection = convertView.findViewById(R.id.tvStudentSectionBadge);
            holder.tvName = convertView.findViewById(R.id.tvStudentName);
            holder.btnEdit = convertView.findViewById(R.id.btnEditStudent);
            holder.btnDelete = convertView.findViewById(R.id.btnDeleteStudent);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Student student = studentList.get(position);
        holder.tvRegNo.setText(student.getRegisterNumber());
        holder.tvSection.setText(student.getSemester() + " • " + student.getDivision());
        holder.tvName.setText(student.getName());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(student);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(student);
        });

        return convertView;
    }
}
