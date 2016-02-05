package geert.berkers.primedice;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Primedice Application Created by Geert on 5-2-2016.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;

    public MessageAdapter(List<Message> messages) {
        mMessages = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_message, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Message m = mMessages.get(position);
        viewHolder.setMessage(m.getSender(), m.getMessage(), m.getTime());
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mSender;
        private TextView mMessageView;
        private TextView mTime;


        public ViewHolder(View itemView) {
            super(itemView);
            mSender = (TextView) itemView.findViewById(R.id.sender);
            mMessageView = (TextView) itemView.findViewById(R.id.message);
            mTime = (TextView) itemView.findViewById(R.id.time);

        }

        public void setMessage(String sender, String message, String time) {
            mSender.setText(sender);
            mMessageView.setText(message);
            mTime.setText(time);
        }

    }
}
