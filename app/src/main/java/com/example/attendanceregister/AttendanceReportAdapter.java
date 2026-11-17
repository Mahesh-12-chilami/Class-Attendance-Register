package com.example.attendanceregister;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * Custom Adapter for displaying Attendance Report list items.
 * Evaluates 75% attendance threshold:
 * - Below 75%: Highlighted in RED, SHORTAGE badge, warning banner visible.
 * - 75% and above: Normal green, ELIGIBLE badge.
 * Displays Semester, Division, Subject, Student Info, and Statistics.
 */
public class AttendanceReportAdapter extends BaseAdapter {

    private final Context context;
    private final List<AttendanceReportModel> reportList;
    private final LayoutInflater inflater;

    public AttendanceReportAdapter(Context context, List<AttendanceReportModel> reportList) {
        this.context = context;
        this.reportList = reportList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return reportList != null ? reportList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return reportList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return reportList.get(position).getStudentId();
    }

    static class ViewHolder {
        TextView tvRegNo;
        TextView tvClassInfo;
        TextView tvBadge;
        TextView tvName;
        TextView tvSubject;
        TextView tvConducted;
        TextView tvAttended;
        TextView tvAbsent;
        TextView tvPercentage;
        TextView tvShortageWarning;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_attendance_report, parent, false);
            holder = new ViewHolder();
            holder.tvRegNo = convertView.findViewById(R.id.tvReportRegNo);
            holder.tvClassInfo = convertView.findViewById(R.id.tvReportClassInfo);
            holder.tvBadge = convertView.findViewById(R.id.tvReportBadge);
            holder.tvName = convertView.findViewById(R.id.tvReportName);
            holder.tvSubject = convertView.findViewById(R.id.tvReportSubject);
            holder.tvConducted = convertView.findViewById(R.id.tvReportConducted);
            holder.tvAttended = convertView.findViewById(R.id.tvReportAttended);
            holder.tvAbsent = convertView.findViewById(R.id.tvReportAbsent);
            holder.tvPercentage = convertView.findViewById(R.id.tvReportPercentage);
            holder.tvShortageWarning = convertView.findViewById(R.id.tvShortageWarning);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AttendanceReportModel item = reportList.get(position);

        holder.tvRegNo.setText(item.getRegisterNumber());
        holder.tvClassInfo.setText(item.getSemester() + " (" + item.getDivision() + ")");
        holder.tvName.setText(item.getStudentName());
        holder.tvSubject.setText("Subject: " + (item.getSubjectName().isEmpty() ? "Selected Subject" : item.getSubjectName()));
        holder.tvConducted.setText(String.valueOf(item.getTotalClasses()));
        holder.tvAttended.setText(String.valueOf(item.getAttendedClasses()));
        holder.tvAbsent.setText(String.valueOf(item.getAbsentClasses()));

        if (item.getTotalClasses() == 0) {
            holder.tvPercentage.setText("N/A");
            holder.tvPercentage.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            holder.tvBadge.setText("NO DATA");
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.badge_eligible));
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            holder.tvShortageWarning.setVisibility(View.GONE);
        } else if (item.isShortage()) {
            // Below 75% -> Highlight in RED, show SHORTAGE badge and warning
            holder.tvPercentage.setText(item.getFormattedPercentage());
            holder.tvPercentage.setTextColor(ContextCompat.getColor(context, R.color.status_absent));
            holder.tvBadge.setText("SHORTAGE (<75%)");
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.badge_shortage));
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.status_absent));
            holder.tvShortageWarning.setVisibility(View.VISIBLE);
        } else {
            // 75% or above -> Normal display, ELIGIBLE
            holder.tvPercentage.setText(item.getFormattedPercentage());
            holder.tvPercentage.setTextColor(ContextCompat.getColor(context, R.color.status_present));
            holder.tvBadge.setText("ELIGIBLE (>=75%)");
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.badge_eligible));
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.status_present));
            holder.tvShortageWarning.setVisibility(View.GONE);
        }

        return convertView;
    }
}
