package com.tuan1611pupu.vishort.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tuan1611pupu.vishort.Activity.LogInActivity;
import com.tuan1611pupu.vishort.Adapter.ReelsAdapter;
import com.tuan1611pupu.vishort.Api.APIService;
import com.tuan1611pupu.vishort.Api.DataService;
import com.tuan1611pupu.vishort.Model.Like;
import com.tuan1611pupu.vishort.Model.NotificationData;
import com.tuan1611pupu.vishort.Model.Reels;
import com.tuan1611pupu.vishort.Model.Save;
import com.tuan1611pupu.vishort.R;
import com.tuan1611pupu.vishort.Utilities.Constants;
import com.tuan1611pupu.vishort.Utilities.PreferenceManager;
import com.tuan1611pupu.vishort.databinding.FragmentHomeBinding;
import com.tuan1611pupu.vishort.databinding.ItemReelsBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements ReelsAdapter.OnReelsVideoAdapterListner {


    private FragmentHomeBinding binding;
    Animation animation;
    private SimpleExoPlayer player;
    private ItemReelsBinding playerBinding;
    Animation rotateAnimation;
    Boolean isLike = false;
    Boolean isSave = false;

    Boolean liked = false;

    private int likes = 0;
    private int saves = 0;
    private ReelsAdapter adapter;
    private ArrayList<Reels> reelsList;
    private int visibale = 0;
    Fragment_comment bottomSheetDialogFragment;
    private Reels reelUser;

    private FirebaseDatabase database;
    private PreferenceManager preferenceManager;

    private HttpProxyCacheServer proxy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            // Lấy đối tượng Reel từ Bundle
            reelUser = (Reels) bundle.getSerializable("reel");
            // Sử dụng đối tượng Reel ở đây
        }
        initView();
        return binding.getRoot();
    }

    private void initView() {
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);
        rotateAnimation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slow_rotate);

        database = FirebaseDatabase.getInstance();
        preferenceManager = new PreferenceManager(getContext());

        bottomSheetDialogFragment = new Fragment_comment();

        // Khởi tạo RecyclerView và adapter
        binding.rvReels.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        // Khởi tạo ExoPlayer
        new PagerSnapHelper().attachToRecyclerView(binding.rvReels);
        player = new SimpleExoPlayer.Builder(getContext()).build();
        // tao bo nho tam
        proxy = new HttpProxyCacheServer.Builder(getContext())
                .maxCacheSize(1024 * 1024 * 1024) // Kích thước tối đa của bộ nhớ cache (1GB)
                .build();

        ///////////////
        reelsList = new ArrayList<>();
        adapter = new ReelsAdapter();
        adapter.setOnReelsVideoAdapterListner(this);
        if(reelUser != null){
            reelsList.add(reelUser);
            adapter.addData(reelsList);
            binding.rvReels.setAdapter(adapter);
        }else {
            loadMoreDataToAdapter();
        }
        binding.rvReels.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        // Resume phát video khi RecyclerView đang ở trạng thái tĩnh (không cuộn)
                        player.setPlayWhenReady(true);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        // Pause phát video khi RecyclerView đang ở trạng thái cuộn
                        player.setPlayWhenReady(false);
                        break;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Tìm ViewHolder đang hiển thị ở giữa màn hình
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                visibale = firstVisiblePosition;
                checkLikeAndCommentCount(visibale);

                for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                    ReelsAdapter.ReelsViewHolder holder = ( ReelsAdapter.ReelsViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                    playerBinding = holder.binding;
                    playerBinding.playerView.setPlayer(player);
                    Animation animation = AnimationUtils.loadAnimation(playerBinding.getRoot().getContext(), R.anim.slow_rotate);
                    playerBinding.lytSound.startAnimation(animation);
                    //playerBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                    Reels currentReel = adapter.getList().get(i);
                   // Uri videoUri = Uri.parse(currentReel.getVideo());
                    /////
                    String proxyUrl = proxy.getProxyUrl(currentReel.getVideo());
                    String userAgent = Util.getUserAgent(getContext(), "Vishort");
                    DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getContext(), userAgent);

                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(Uri.parse(proxyUrl)));
                    player.clearMediaItems();
                    //player.setMediaItem(MediaItem.fromUri(videoUri));
                    player.prepare(mediaSource);

                }

            }


        });

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if(playbackState == Player.STATE_ENDED) {
                    player.seekToDefaultPosition(); // quay về vị trí đầu tiên của video
                    player.setPlayWhenReady(true); // phát lại video
                }
            }
            @Override
            public void onIsLoadingChanged(boolean isLoading) {
                if (isLoading) {
                    // Player đang tải nội dung
                    binding.buffering.setVisibility(View.VISIBLE);
                    player.setPlayWhenReady(false);
                     Log.d("VALIDATION_VIDEO","VALIDATION_VIDEO_02");
                } else {
                    // Player đã tải xong nội dung
                    binding.buffering.setVisibility(View.GONE);
                    player.setPlayWhenReady(true);
                }
            }
        });

    }



    private void loadMoreDataToAdapter() {
        DatabaseReference usersRef = database.getReference("Reels");

        // Sử dụng phương thức addListenerForSingleValueEvent để lấy dữ liệu một lần duy nhất
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Đọc dữ liệu của từng user từ DataSnapshot và chuyển đổi thành đối tượng User
                        Reels reels = userSnapshot.getValue(Reels.class);
                        reelsList.add(reels);
                    }
                    Log.d("listReel",reelsList.size()+"!");
                    adapter.addData(reelsList);
                    binding.rvReels.setAdapter(adapter);

                    // Ở đây, userList chứa danh sách các đối tượng User
                    // Tiếp tục xử lý dữ liệu theo nhu cầu của bạn
                } else {
                    // Dữ liệu không tồn tại hoặc danh sách user rỗng
                    Log.d("listReel",reelsList.size()+"!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi khi lấy dữ liệu bị hủy
                Log.d("listReel",reelsList.size()+"!2");
            }
        });

    }


    private void loadVideo(int pos){
        playerBinding = adapter.getBindingAtPosition(pos);
        playerBinding.playerView.setPlayer(player);
        Animation animation = AnimationUtils.loadAnimation(playerBinding.getRoot().getContext(), R.anim.slow_rotate);
        playerBinding.lytSound.startAnimation(animation);
        //playerBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        Reels currentReel = adapter.getList().get(pos);
        Uri videoUri = Uri.parse(currentReel.getVideo());
        player.clearMediaItems();
        player.setMediaItem(MediaItem.fromUri(videoUri));
        player.prepare();
        player.setPlayWhenReady(true);

    }

    @Override
    public void onResume() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
        }
        super.onDestroy();
    }


    // Hàm callback khi người dùng bấm vào item video
    @Override
    public void onItemClick(ItemReelsBinding reelsBinding, int pos, int type) {

        if (player.isPlaying()) {
            // Nếu ExoPlayer đang phát, tạm dừng và lưu lại vị trí hiện tại
            player.setPlayWhenReady(false);
            binding.playing.setVisibility(View.VISIBLE);
            Log.d("VALIDATION_TTVideo","VALIDATION_TTVideo_02");
        } else {
            // Nếu ExoPlayer đang tạm dừng, tiếp tục phát từ vị trí hiện tại
            binding.playing.setVisibility(View.GONE);
            player.setPlayWhenReady(true);
        }

    }


    // Hàm callback khi video được chạm đôi
    @Override
    public void onDoubleClick(Reels model, MotionEvent event, ItemReelsBinding binding) {
        // Xử lý sự kiện khi người dùng chạm đôi vào video
    }

    // Hàm callback khi người dùng bấm nút like
    @Override
    public void onClickLike(ItemReelsBinding reelsBinding, int pos) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Xử lý sự kiện khi người dùng bấm vào nút like video
            playerBinding = reelsBinding;
            Reels currentReel = adapter.getList().get(pos);


            DatabaseReference reelsRef = FirebaseDatabase.getInstance()
                    .getReference("Reels")
                    .child(currentReel.getReelsId());

            if (isLike && likes > 0) {
                // Nếu người dùng đã thích và số lượng like > 0

                DataService dataService = APIService.getService();
                Call<Void> call = dataService.deleteLike(preferenceManager.getString(Constants.KEY_USER_ID),currentReel.getReelsId());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Xử lý khi cập nhật thành công
                            likes--; // Giảm số lượng like
                            Map<String, Object> likesCountUpdates = new HashMap<>();
                            likesCountUpdates.put("likes", likes);
                            reelsRef.updateChildren(likesCountUpdates)
                                    .addOnSuccessListener(aVoid1 -> {
                                        // Xử lý khi cập nhật thành công số lượng like
                                        playerBinding.likeCount.setText(String.valueOf(likes));
                                    })
                                    .addOnFailureListener(e -> {
                                        // Xử lý khi cập nhật số lượng like thất bại
                                    });

                        } else {
                            // Xử lý lỗi khi yêu cầu thất bại

                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // Xử lý lỗi khi yêu cầu thất bại
                    }
                });


            } else {

                Like like = new Like();
                UUID uuid = UUID.randomUUID();
                like.setLikeId(uuid.toString());
                like.setUserId(preferenceManager.getString(Constants.KEY_USER_ID));
                like.setReelsId(currentReel.getReelsId());
                if (liked) {
                    DataService dataService = APIService.getService();
                    Call<Void> call = dataService.addLike(like);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                // Xử lý khi cập nhật thành công
                                likes++; // Tăng số lượng like
                                Map<String, Object> likesCountUpdates = new HashMap<>();
                                likesCountUpdates.put("likes", likes);
                                reelsRef.updateChildren(likesCountUpdates)
                                        .addOnSuccessListener(aVoid1 -> {
                                            // Xử lý khi cập nhật thành công số lượng like
                                            playerBinding.likeCount.setText(String.valueOf(likes));
                                        })
                                        .addOnFailureListener(e -> {
                                            // Xử lý khi cập nhật số lượng like thất bại
                                        });

                            } else {
                                // Xử lý lỗi khi yêu cầu thất bại

                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Xử lý lỗi khi yêu cầu thất bại
                        }
                    });

                }else {
                    DataService dataService = APIService.getService();
                    Call<Void> call = dataService.addLike(like);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                String userId = currentReel.getReelsBy();
                                addNotification(uuid.toString(), userId);
                                // Xử lý khi cập nhật thành công
                                likes++; // Tăng số lượng like
                                Map<String, Object> likesCountUpdates = new HashMap<>();
                                likesCountUpdates.put("likes", likes);
                                reelsRef.updateChildren(likesCountUpdates)
                                        .addOnSuccessListener(aVoid1 -> {
                                            // Xử lý khi cập nhật thành công số lượng like
                                            playerBinding.likeCount.setText(String.valueOf(likes));
                                        })
                                        .addOnFailureListener(e -> {
                                            // Xử lý khi cập nhật số lượng like thất bại
                                        });

                            } else {
                                // Xử lý lỗi khi yêu cầu thất bại

                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Xử lý lỗi khi yêu cầu thất bại
                        }
                    });
                }

            }
        }else {
            Intent intent = new Intent(getContext(), LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }





    }

    // Hàm callback khi người dùng bấm vào tên người đăng video
    @Override
    public void onClickUser(Reels reel) {
        // Xử lý sự kiện khi người dùng bấm vào tên người đăng
        String userId = reel.getReelsBy();
        if (!userId.equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
            player.setPlayWhenReady(false);
            UserProfileFragment fragment = new UserProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("userId",userId);
            fragment.setArguments(bundle);

            FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        }
    }

    // Hàm callback khi người dùng bấm vào nút comment
    @Override
    public void onClickComments(Reels reels) {
        // Xử lý sự kiện khi người dùng bấm vào nút comment
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Bundle bundle = new Bundle();
            bundle.putString("reelId", reels.getReelsId());
            bundle.putString("reelBy", reels.getReelsBy());// Chèn giá trị vào Bundle
            bottomSheetDialogFragment.setArguments(bundle);
            bottomSheetDialogFragment.show(getChildFragmentManager(),bottomSheetDialogFragment.getTag());
        }else {
            Intent intent = new Intent(getContext(), LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }

    // Hàm callback khi người dùng bấm vào nút share
    @Override
    public void onClickShare(Reels reel) {

    }

    @Override
    public void onClickSave(ItemReelsBinding reelsBinding, int pos) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            playerBinding = reelsBinding;
            Reels currentReel = adapter.getList().get(pos);
            DatabaseReference reelsRef = FirebaseDatabase.getInstance()
                    .getReference("Reels")
                    .child(currentReel.getReelsId());

            if (isSave && saves > 0) {
                // Nếu người dùng đã thích và số lượng like > 0
                DataService dataService = APIService.getService();
                Call<Void> call = dataService.deleteSave(preferenceManager.getString(Constants.KEY_USER_ID),currentReel.getReelsId());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Xử lý khi cập nhật thành công
                            saves--; // Giảm số lượng like
                            Map<String, Object> likesCountUpdates = new HashMap<>();
                            likesCountUpdates.put("saves", saves);
                            reelsRef.updateChildren(likesCountUpdates)
                                    .addOnSuccessListener(aVoid1 -> {
                                        // Xử lý khi cập nhật thành công số lượng like
                                        playerBinding.saveCount.setText(String.valueOf(saves));
                                    })
                                    .addOnFailureListener(e -> {
                                        // Xử lý khi cập nhật số lượng like thất bại
                                    });

                        } else {
                            // Xử lý lỗi khi yêu cầu thất bại

                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // Xử lý lỗi khi yêu cầu thất bại
                    }
                });


            } else {

                Save save = new Save();
                UUID uuid = UUID.randomUUID();
                save.setSaveId(uuid.toString());
                save.setUserId(preferenceManager.getString(Constants.KEY_USER_ID));
                save.setReelId(currentReel.getReelsId());

                DataService dataService = APIService.getService();
                Call<Void> call = dataService.saveVideo(save);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Xử lý khi cập nhật thành công
                            saves++; // Tăng số lượng like
                            Map<String, Object> likesCountUpdates = new HashMap<>();
                            likesCountUpdates.put("saves", saves);
                            reelsRef.updateChildren(likesCountUpdates)
                                    .addOnSuccessListener(aVoid1 -> {
                                        // Xử lý khi cập nhật thành công số lượng like
                                        playerBinding.saveCount.setText(String.valueOf(saves));
                                    })
                                    .addOnFailureListener(e -> {
                                        // Xử lý khi cập nhật số lượng like thất bại
                                    });

                        } else {
                            // Xử lý lỗi khi yêu cầu thất bại

                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // Xử lý lỗi khi yêu cầu thất bại
                    }
                });


            }
        }else {
            Intent intent = new Intent(getContext(), LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


    }


    @Override
    public void test() {
        if(visibale == 0){
            loadVideo(visibale);
            checkLikeAndCommentCount(visibale);
        }
    }

    private void checkLikeAndCommentCount(int pos) {
        playerBinding = adapter.getBindingAtPosition(pos);
        Reels currentReel = adapter.getList().get(pos);
        DatabaseReference reelsRef = FirebaseDatabase.getInstance()
                .getReference("Reels")
                .child(currentReel.getReelsId());

        reelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Reels reels = dataSnapshot.getValue(Reels.class);
                    if (reels != null) {
                        likes = reels.getLikes();
                        saves = reels.getSaves();
                        int commentCount = reels.getComments();
                        playerBinding.likeCount.setText(String.valueOf(likes));
                        playerBinding.commentCount.setText(String.valueOf(commentCount));
                        playerBinding.saveCount.setText(String.valueOf(saves));

                    }
                } else {
                    // Xử lý trường hợp không tìm thấy tài liệu được yêu cầ
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi khi lấy dữ liệu bị hủy
            }
        });
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference likeRef = FirebaseDatabase.getInstance()
                    .getReference("Like")
                    .child(currentReel.getReelsId());

            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Boolean isLiked = dataSnapshot.child(preferenceManager.getString(Constants.KEY_USER_ID)).getValue(Boolean.class);
                        if (isLiked != null) {
                            liked = true;
                            if (isLiked) {
                                // userIdToCheck có giá trị true trong danh sách like
                                // Thực hiện các hành động khi người dùng đã like
                                playerBinding.like.setLiked(true);
                                isLike = true;
                                 Log.d("VALIDATION_LIKE","VALIDATION_LIKE_01");
                            } else {
                                // userIdToCheck có giá trị false trong danh sách like
                                // Thực hiện các hành động khi người dùng đã unlike
                                playerBinding.like.setLiked(false);
                                isLike = false;
                                 Log.d("VALIDATION_LIKE","VALIDATION_LIKE_01");
                            }
                        } else {
                            // userIdToCheck không tồn tại trong danh sách like hoặc có giá trị null
                            // Thực hiện các hành động khi không tìm thấy userId
                            playerBinding.like.setLiked(false);
                            isLike = false;
                            liked = false;
                        }
                    } else {
                        // Node LikeId_1 không tồn tại hoặc không có dữ liệu
                        // Thực hiện các hành động khi không tìm thấy node
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Xử lý lỗi nếu có
                }
            });

            DatabaseReference saveRef = FirebaseDatabase.getInstance()
                    .getReference("SaveVideo")
                    .child(currentReel.getReelsId());

            saveRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Boolean isSaved = dataSnapshot.child(preferenceManager.getString(Constants.KEY_USER_ID)).getValue(Boolean.class);
                        if (isSaved != null) {
                            if (isSaved) {
                                // userIdToCheck có giá trị true trong danh sách like
                                // Thực hiện các hành động khi người dùng đã like
                                playerBinding.save.setLiked(true);
                                isSave = true;
                            } else {
                                // userIdToCheck có giá trị false trong danh sách like
                                // Thực hiện các hành động khi người dùng đã unlike
                                playerBinding.save.setLiked(false);
                                isSave = false;
                                 Log.d("VALIDATION_SAVE_VIDEO","VALIDATION_SAVE_VIDEO_01");
                            }
                        } else {
                            // userIdToCheck không tồn tại trong danh sách like hoặc có giá trị null
                            // Thực hiện các hành động khi không tìm thấy userId
                            playerBinding.save.setLiked(false);
                            isSave = false;
                             Log.d("VALIDATION_SAVE_VIDEO","VALIDATION_SAVE_VIDEO_01");
                        }
                    } else {
                        // Node LikeId_1 không tồn tại hoặc không có dữ liệu
                        // Thực hiện các hành động khi không tìm thấy node
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Xử lý lỗi nếu có
                }
            });
        }



    }
    private void addNotification(String liked,String userId){
        NotificationData notificationData = new NotificationData();
        UUID uuid = UUID.randomUUID();
        notificationData.setNotificationId(uuid.toString());
        notificationData.setUserId(userId);
        notificationData.setActionId(liked);
        notificationData.setNotificationBy(preferenceManager.getString(Constants.KEY_USER_ID));
        notificationData.setIsRead(0);
        notificationData.setTimestamp(new Date().getTime());
        notificationData.setActionType("like");

        DataService dataService = APIService.getService();
        Call<Void> call = dataService.addNotification(notificationData);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

                } else {
                    // Xử lý lỗi khi yêu cầu thất bại

                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Xử lý lỗi khi yêu cầu thất bại
            }
        });

    }





}
