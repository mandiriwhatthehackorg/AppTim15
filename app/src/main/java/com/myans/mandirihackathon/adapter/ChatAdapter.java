package com.myans.mandirihackathon.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myans.mandirihackathon.R;
import com.myans.mandirihackathon.model.ChatModel;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ExampleHolder>{

    ArrayList<ChatModel> listChat;

    private static final int VIEW_TYPE_USER_MESSAGE = 1;
    private static final int VIEW_TYPE_SYSTEM_MESSAGE = 2;
    private static final int VIEW_TYPE_SYSTEM_TYPING= 3;


    public ChatAdapter() {
    }

    public ArrayList<ChatModel> getListChat() {
        return listChat;
    }

    public void setListChat(ArrayList<ChatModel> listChat) {
        this.listChat = listChat;
    }

    public void removeLastItem(){
        listChat.remove(listChat.size()-1);
        return;
    }

    @NonNull
    @Override
    public ChatAdapter.ExampleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_USER_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new ExampleHolder(view);
        } else if (viewType == VIEW_TYPE_SYSTEM_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ExampleHolder(view);
        } else if (viewType == VIEW_TYPE_SYSTEM_TYPING) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_typing, parent, false);
            return new ExampleHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ExampleHolder holder, int position) {
        ChatModel currentChat = listChat.get(position);
        if(currentChat.getFrom() == ChatModel.SYSTEM_TYPING_INPUT)
            holder.tv_send_time.setText("");
        else
            holder.tv_send_time.setText(currentChat.getSendTime());
        holder.tv_message.setText(currentChat.getMessage());

    }

    @Override
    public int getItemCount() {
        return listChat.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel chat = listChat.get(position);

        if (chat.getFrom() == ChatModel.USER_INPUT) {
            // If the current user is the sender of the message
            return VIEW_TYPE_USER_MESSAGE;
        } else if (chat.getFrom() == ChatModel.SYSTEM_INPUT) {
            // If some other user sent the message
            return VIEW_TYPE_SYSTEM_MESSAGE;
        }
        else{
            return VIEW_TYPE_SYSTEM_TYPING;
        }
    }

    public class ExampleHolder extends RecyclerView.ViewHolder {

        public TextView tv_message;
        public TextView tv_send_time;

        public ExampleHolder(@NonNull View itemView) {
            super(itemView);
            tv_message = itemView.findViewById(R.id.text_message_body);
            tv_send_time = itemView.findViewById(R.id.text_message_time);
        }
    }
}
