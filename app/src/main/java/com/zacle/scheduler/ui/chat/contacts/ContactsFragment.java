package com.zacle.scheduler.ui.chat.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zacle.scheduler.R;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.UserListItemConverter;
import co.chatsdk.ui.contacts.UsersListAdapter;
import co.chatsdk.ui.main.BaseFragment;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class ContactsFragment extends BaseFragment {

    private static final String TAG = "ContactsFragment";

    /** The text color that the adapter will use, Use -1 to set adapter to default color.*/
    protected int textColor = -1991;

    protected UsersListAdapter adapter;
    protected ProgressBar progressBar;
    protected RecyclerView recyclerView;

    private boolean showProfileActivityTransitionStarted = false;

    protected PublishSubject<User> onClickSubject = PublishSubject.create();
    protected PublishSubject<User> onLongClickSubject = PublishSubject.create();
    protected Disposable listOnClickListenerDisposable;
    protected Disposable listOnLongClickListenerDisposable;

    /** Users that will be used to fill the adapter, This could be set manually or it will be filled when loading users for
     * loading */
    protected List<User> sourceUsers = new ArrayList<>();

    /** Used when the fragment is shown as a dialog*/
    protected String title = "";

    protected Object extraData = "";

    /** Creates a new contact dialog.
     * @param threadID - The id of the thread that his users is the want you want to show.
     * @param title - The title of the dialog.
     */
    public static ContactsFragment newThreadUsersDialogInstance(String threadID, String title) {
        ContactsFragment f = new ContactsFragment();
        f.setTitle(title);
        f.setExtraData(threadID);
        Bundle b = new Bundle();
        f.setArguments(b);

        return f;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setExtraData(Object extraData) {
        this.extraData = extraData;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterContactsChanged())
                .subscribe(networkEvent -> loadData(true)));

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated))
                .subscribe(networkEvent -> loadData(true)));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainView = inflater.inflate(activityLayout(), null);

        initViews();

        loadData(true);

        return mainView;
    }

    protected @LayoutRes
    int activityLayout() {
        return R.layout.fragment_contacts;
    }

    public void initViews() {
        recyclerView = mainView.findViewById(R.id.recycler_contacts);

        progressBar = mainView.findViewById(R.id.progress_bar);

        // Create the adapter only if null this is here so we wont
        // override the adapter given from the extended class with setAdapter.
        adapter = new UsersListAdapter();

        setTextColor(textColor);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    public void loadData (final boolean force) {
        final ArrayList<User> originalUserList = new ArrayList<>(sourceUsers);
        reloadData();
        if (!originalUserList.equals(sourceUsers) || force) {
            adapter.setUsers(UserListItemConverter.toUserItemList(sourceUsers), true);
            for (User u : sourceUsers) {
                System.out.println("Update contacts " + u.getName());
            }
        }
        setupListClickMode();
    }

    @Override
    public void clearData() {
        if (adapter != null) {
            adapter.clear();
        }
    }

    protected void setupListClickMode() {
        if (listOnClickListenerDisposable != null) {
            listOnClickListenerDisposable.dispose();
        }
        listOnClickListenerDisposable = adapter.onClickObservable().subscribe(o -> {
            if (o instanceof User) {
                final User clickedUser = (User) o;

                onClickSubject.onNext(clickedUser);

                if (!showProfileActivityTransitionStarted) {
                    ChatSDK.ui().startProfileActivity(getContext(), clickedUser.getEntityID());
                    showProfileActivityTransitionStarted = true;
                }
            }
        });

        if (listOnLongClickListenerDisposable != null) {
            listOnLongClickListenerDisposable.dispose();
        }
        listOnLongClickListenerDisposable = adapter.onLongClickObservable().subscribe(o -> {
            if (o instanceof User) {
                onLongClickSubject.onNext((User) o);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        showProfileActivityTransitionStarted = false;
        loadData(true);
    }

    @Override
    public void reloadData() {
        sourceUsers.clear();

        Thread thread = DaoCore.fetchEntityWithEntityID(Thread.class, extraData);
        if (thread != null) {
            // Remove the current user from the list.
            List<User> users = thread.getUsers();
            for (User u : users) {
                if (!u.isMe()) {
                    sourceUsers.add(u);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public Observable<User> onClickObservable () {
        return onClickSubject;
    }

    public Observable<User> onLongClickObservable () {
        return onLongClickSubject;
    }
}
