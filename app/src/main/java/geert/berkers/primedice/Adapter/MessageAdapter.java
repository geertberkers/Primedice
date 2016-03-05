package geert.berkers.primedice.Adapter;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import geert.berkers.primedice.Activity.MainActivity;
import geert.berkers.primedice.Activity.PlayerInformationActivity;
import geert.berkers.primedice.Data.Message;
import geert.berkers.primedice.DataHandler.MyClickableSpan;
import geert.berkers.primedice.Fragment.ChatFragment;
import geert.berkers.primedice.R;

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
        viewHolder.setMessage(mMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    public void setUpdatedList(List<Message> updatedList) {
        this.mMessages = updatedList;
        this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView tag;
        private final TextView type;
        private final TextView sender;
        private final TextView message;
        private final TextView toUsername;
        private final TextView time;

        public ViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.btnUserInfo);

            tag = (TextView) itemView.findViewById(R.id.tag);
            type = (TextView) itemView.findViewById(R.id.type);
            sender = (TextView) itemView.findViewById(R.id.sender);
            message = (TextView) itemView.findViewById(R.id.message);
            toUsername = (TextView) itemView.findViewById(R.id.toUsername);
            time = (TextView) itemView.findViewById(R.id.time);
        }

        // Prepare message to show
        public void setMessage(final Message message) {

            String messageText = message.getMessage();
            SpannableString spannableString = new SpannableString(messageText);

            setSpannableString(spannableString, messageText, "u:", MyClickableSpan.USER);
            setSpannableString(spannableString, messageText, "b:", MyClickableSpan.BET);
            setSpannableString(spannableString, messageText, "bet:", MyClickableSpan.BET);

            this.message.setText(spannableString);
            this.message.setLinkTextColor(Color.BLUE);
            this.message.setMovementMethod(LinkMovementMethod.getInstance());

            this.sender.setText(message.getSender());
            this.time.setText(message.getLocalTime());

            this.toUsername.setVisibility(View.GONE);

            String toUser = message.getToUsername();
            if(toUser != null){
                if(!toUser.equals("null")) {
                    toUser = "to " + message.getToUsername();
                    this.toUsername.setText(toUser);
                    this.toUsername.setVisibility(View.VISIBLE);
                    this.message.setBackgroundResource(R.drawable.chat_background_pm);
                }
            }
            tag.setVisibility(View.VISIBLE);
            type.setVisibility(View.VISIBLE);

            switch (message.getTag()) {
                case Message.VIP:
                    tag.setText("VIP");
                    tag.setTextColor(Color.parseColor("#ffd700"));
                    break;
                case Message.MOD:
                    tag.setText("M");
                    tag.setTextColor(Color.RED);
                    break;
                case Message.ADMIN:
                    tag.setText("A");
                    tag.setTextColor(Color.RED);
                    break;
                case Message.SUPPORT:
                    tag.setText("S");
                    tag.setTextColor(Color.RED);
                    break;
                default:
                    tag.setVisibility(View.GONE);
                    break;
            }

            switch (message.getType()) {
                case Message.PM:
                    type.setText("PM");
                    type.setTextColor(Color.RED);
                    setSenderHandlers(message.getSender());
                    break;
                case Message.BOT:
                    type.setText("BOT");
                    type.setTextColor(Color.RED);
                    image.setImageResource(R.drawable.bot);
                    break;
                default:
                    type.setVisibility(View.GONE);
                    setSenderHandlers(message.getSender());
                    break;
            }
        }

        // Set image and onClickListener for users.
        private void setSenderHandlers(final String sender) {
            this.image.setImageResource(R.drawable.info);

            this.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent playerInfoIntent = new Intent(v.getContext(), PlayerInformationActivity.class);
                    playerInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    playerInfoIntent.putExtra("playerName", sender);
                    v.getContext().startActivity(playerInfoIntent);
                }
            });

            this.sender.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu menu = new PopupMenu(v.getContext(), v);

                    menu.getMenu().add("MESSAGE");
                    menu.getMenu().add("TIP USER");
                    menu.getMenu().add(ChatFragment.getUserIgnoreString(sender));
                    menu.show();

                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.toString()) {
                                case "MESSAGE":     MainActivity.sendPM(null, sender);          break;
                                case "TIP USER":    MainActivity.tipUser(null, sender);         break;
                                case "IGNORE":      ChatFragment.ignoreOrUnignoreUser(sender);  break;
                                case "UNIGNORE":    ChatFragment.ignoreOrUnignoreUser(sender);  break;
                                default:            return false;
                            }
                            return true;
                        }
                    });
                }
            });
        }

        // Make users and bets clickable
        private void setSpannableString(SpannableString spannableString, String messageText, String split, int clickableObject) {
            for (int startIndex = -1; (startIndex = messageText.toLowerCase().indexOf(split, startIndex + 1)) != -1; ) {
                String value = messageText.substring(startIndex).toLowerCase();

                if (value.contains(" ")) {
                    value = value.substring(0, value.indexOf(" "));
                }

                int endIndex = startIndex + value.length();

                spannableString.setSpan(new MyClickableSpan(clickableObject, value.replace(split, "")), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
