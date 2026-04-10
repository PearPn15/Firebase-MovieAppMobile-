package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private BottomNavigationView bottomNav;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Kiểm tra đăng nhập
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        // RecyclerView
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, movieList, movie -> {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra("movieId", movie.getId());
            intent.putExtra("movieTitle", movie.getTitle());
            intent.putExtra("movieDescription", movie.getDescription());
            intent.putExtra("movieGenre", movie.getGenre());
            intent.putExtra("movieDuration", movie.getDuration());
            intent.putExtra("movieRating", movie.getRating());
            intent.putExtra("moviePosterUrl", movie.getPosterUrl());
            intent.putExtra("moviePrice", movie.getPrice());
            if (movie.getShowtimes() != null) {
                intent.putStringArrayListExtra("movieShowtimes", new ArrayList<>(movie.getShowtimes()));
            }
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(movieAdapter);

        // Bottom Navigation
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_movies) {
                recyclerView.setVisibility(View.VISIBLE);
                return true;
            } else if (id == R.id.nav_tickets) {
                startActivity(new Intent(this, MyTicketsActivity.class));
                return true;
            }
            return false;
        });

        loadMovies();
    }

    private void loadMovies() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("movies")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Xóa dữ liệu cũ và seed lại
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                    seedSampleMovies();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void seedSampleMovies() {
        // Tạo dữ liệu mẫu vào Firestore lần đầu
        List<Movie> samples = new ArrayList<>();
        samples.add(new Movie(null, "Avengers: Endgame",
                "Sau sự kiện Thanos tiêu diệt một nửa vũ trụ, các Avengers còn lại phải tập hợp lại để đảo ngược hành động của Thanos và khôi phục sự cân bằng cho vũ trụ.",
                "Hành động / Phiêu lưu", 181, 8.4f,
                "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
                Arrays.asList("09:00", "11:30", "14:00", "17:00", "20:00", "22:30"), 85000L));

        samples.add(new Movie(null, "Spider-Man: No Way Home",
                "Peter Parker nhờ Doctor Strange giúp mọi người quên mình là Spider-Man, nhưng phép thuật bị gián đoạn khiến các phản diện từ vũ trụ song song ùa vào.",
                "Hành động / Viễn tưởng", 148, 8.2f,
                "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg",
                Arrays.asList("10:00", "13:00", "16:00", "19:30", "22:00"), 90000L));

        samples.add(new Movie(null, "Top Gun: Maverick",
                "Sau hơn 30 năm phục vụ, Pete 'Maverick' Mitchell trở lại huấn luyện đội phi công trẻ cho một nhiệm vụ đặc biệt cực kỳ nguy hiểm.",
                "Hành động / Kịch tính", 180, 8.3f,
                "https://image.tmdb.org/t/p/w500/62HCnUTziyWcpDaBO2i1DX17ljH.jpg",
                Arrays.asList("09:30", "12:00", "15:00", "18:30", "21:00"), 80000L));

        samples.add(new Movie(null, "The Batman",
                "Trong năm thứ hai trở thành Batman, Bruce Wayne điều tra một kẻ giết người hàng loạt gọi là Riddler, người đang nhắm vào tầng lớp thượng lưu Gotham.",
                "Tội phạm / Hành động", 176, 7.8f,
                "https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg",
                Arrays.asList("10:30", "14:00", "17:30", "21:00"), 85000L));

        samples.add(new Movie(null, "Doctor Strange in the Multiverse of Madness",
                "Doctor Strange khám phá Đa Vũ Trụ cùng người bạn đồng hành mới America Chavez, trong khi đối mặt với mối đe dọa khủng khiếp từ Scarlet Witch.",
                "Hành động / Kinh dị", 126, 6.9f,
                "https://image.tmdb.org/t/p/w500/9Gtg2DzBhmYamXBS1hKAhiwbBKS.jpg",
                Arrays.asList("09:00", "11:30", "14:30", "17:30", "20:30"), 85000L));

        // Lưu tất cả vào Firestore
        for (Movie movie : samples) {
            db.collection("movies").add(movie)
                    .addOnSuccessListener(docRef -> {
                        movie.setId(docRef.getId());
                        movieList.add(movie);
                        movieAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.GONE);
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
