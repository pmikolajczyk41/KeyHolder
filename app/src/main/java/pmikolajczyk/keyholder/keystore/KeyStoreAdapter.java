package pmikolajczyk.keyholder.keystore;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import pmikolajczyk.keyholder.R;

class KeyStoreAdapter extends RecyclerView.Adapter<KeyStoreAdapter.KeyStoreViewHolder> {
    private Context context;
    private List<Key> Keys = new ArrayList<>();
    private final LayoutInflater inflater;
    private KeyActionsListener keyActionsListener;

    KeyStoreAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    interface KeyActionsListener {
        void onCopyUsername(String username);
        void onCopyPassword(String password, int valueLength);
        void onEditKey(Key key);
        void onDeleteKey(Key key);
        void onGo(Key key);
    }

    public void setKeyActionsListener(KeyActionsListener keyActionsListener) {
        this.keyActionsListener = keyActionsListener;
    }

    class KeyStoreViewHolder extends RecyclerView.ViewHolder {
        TextView textViewKeyName;
        Button buttonGo;
        Button buttonOtherActions;
        Key key;

        KeyStoreViewHolder(View itemView) {
            super(itemView);
            textViewKeyName = itemView.findViewById(R.id.textViewKeyName);
            buttonOtherActions = itemView.findViewById(R.id.buttonOtherActions);

            buttonGo = itemView.findViewById(R.id.buttonGo);
            if(key != null && ( key.loginPage == null || key.loginPage.equals("")))
                buttonGo.setEnabled(false);
        }

        public void setKey(Key key) {
            this.key = key;
            textViewKeyName.setText(key.name);
        }
    }

    @NonNull
    @Override
    public KeyStoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_layout, parent, false);
        return new KeyStoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeyStoreViewHolder holder, int position) {
        holder.setKey(Keys.get(position));
        setViewHolderActions(holder);
    }

    private void setViewHolderActions(KeyStoreViewHolder holder) {
        setButtonGo(holder);
        setButtonActions(holder);
    }

    private void setButtonGo(KeyStoreViewHolder holder) {
        holder.buttonGo.setOnClickListener(item -> {
            keyActionsListener.onGo(holder.key);
        });
    }

    private void setButtonActions(KeyStoreViewHolder holder) {
        PopupMenu popupMenu = new PopupMenu(context, holder.buttonOtherActions);
        popupMenu.inflate(R.menu.other_actions);

        showPopupIcons(popupMenu);

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.other_actions_copy_username:
                    keyActionsListener.onCopyUsername(holder.key.username);
                    break;
                case R.id.other_actions_copy_password:
                    keyActionsListener.onCopyPassword(holder.key.value, holder.key.valueLength);
                    break;
                case R.id.other_actions_edit_key:
                    keyActionsListener.onEditKey(holder.key);
                    break;
                case R.id.other_actions_delete_key:
                    keyActionsListener.onDeleteKey(holder.key);
                    break;
            }
            return true;
        });

        holder.buttonOtherActions.setOnClickListener(v -> {
            popupMenu.show();
        });
    }

    private void showPopupIcons(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return Keys.size();
    }

    public void setListContent(List<Key> Keys) {
        this.Keys = Keys;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void add(int position, Key key) {
        Keys.add(position, key);
        notifyItemInserted(position);
    }

    public void add(Key key) {
        add(Keys.size(), key);
    }

    public void remove(String name) {
        for (int position = 0; position < Keys.size(); position++) {
            if (Keys.get(position).name.equals(name)) {
                remove(position);
                break;
            }
        }
    }

    public void remove(int position) {
        Keys.remove(position);
        notifyItemRemoved(position);
    }

    public void removeAll() {
        int oldLength = Keys.size();
        Keys = new ArrayList<>();
        notifyItemRangeRemoved(0, oldLength);
    }
}
