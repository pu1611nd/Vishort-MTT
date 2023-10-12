package com.tuan1611pupu.vishort.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tuan1611pupu.vishort.Adapter.SearchAdapter;
import com.tuan1611pupu.vishort.Model.User;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class SearchUserFragment extends Fragment {

    private RecyclerView rcListUser;
    private ArrayList<User> list;
    private SearchAdapter adapter;
    private PreferenceManager preferenceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seach_user, container, false);
        rcListUser = view.findViewById(R.id.rvSearchUser);

        preferenceManager = new PreferenceManager(getContext());
        list = new ArrayList<>();
        adapter = new SearchAdapter();
        adapter.setOnItemClick(new SearchAdapter.OnItemUserClick() {
            @Override
            public void onClick(User user) {
                // Xử lý sự kiện khi người dùng click vào mục user
                UserProfileFragment fragment = new UserProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("userId",user.getId());
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        getdata();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        rcListUser.setLayoutManager(linearLayoutManager);

        return  view;
    }

    private void getdata(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference usersReference = firebaseDatabase.getReference().child(Constants.KEY_COLLECTION_USERS);

        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                List<User> list = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();

                    if (currentUserId.equals(userId)) {
                        continue;
                    }

                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(userId);
                        list.add(user);
                    }
                }

                if (list.size() > 0) {
                    adapter.addData(list);
                    rcListUser.setAdapter(adapter);
                } else {
                    // Xử lý khi danh sách rỗng
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình truy vấn
            }
        });



    }

}
