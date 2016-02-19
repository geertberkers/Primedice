package geert.berkers.primedice;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public void setUpdatedList(List<Message> updatedList){
        this.mMessages = updatedList;
        this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mSender;
        private TextView mMessageView;
        private TextView mTime;
        private ImageView mUserInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            mSender = (TextView) itemView.findViewById(R.id.sender);
            mMessageView = (TextView) itemView.findViewById(R.id.message);
            mTime = (TextView) itemView.findViewById(R.id.time);
            mUserInfo = (ImageView) itemView.findViewById(R.id.btnUserInfo);
        }

        //TODO: Use correct time (get timezone)
        //TODO: Make bets and users clickable
        public void setMessage(final String sender, String message, String time) {

            String showMessage = message.replace("\n"," ");

            mSender.setText(sender);
            mMessageView.setText(showMessage);
            mTime.setText(time.substring(11,16) + " GMT");

            if (sender.equals("Tipsy") || sender.equals("Mute Bot")) {
                mUserInfo.setImageResource(R.drawable.bot);
            } else {
                mSender.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: Popup Message/Tip/Ignore
                    }
                });


                mUserInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent playerInfoIntent = new Intent(v.getContext(), PlayerInformationActivity.class);
                        playerInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        playerInfoIntent.putExtra("playerName", sender);
                        v.getContext().startActivity(playerInfoIntent);
                    }
                });
            }
        }
    }
}
