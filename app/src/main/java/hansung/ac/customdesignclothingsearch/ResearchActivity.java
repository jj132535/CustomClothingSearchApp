package hansung.ac.customdesignclothingsearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResearchActivity extends AppCompatActivity {

    private GridLayout productContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_research);

        productContainer = findViewById(R.id.productContainer1);

        Intent intent = getIntent();
        String itemsJson = intent.getStringExtra("items");

        if (itemsJson != null) {
            try {
                JSONArray items = new JSONArray(itemsJson);
                displayProducts(items);
            } catch (JSONException e) {
                Log.e("ResearchActivity", "Error parsing JSON data", e);
            }
        }
    }

    //검색 결과로 받아온 제품 정보 표시
    private void displayProducts(JSONArray items) {
        try {
            for (int i = 0; i < items.length() && i < 8; i++) {
                JSONObject item = items.getJSONObject(i);
                String title = item.getString("title").replaceAll("<.*?>", ""); // HTML 태그 제거
                String image = item.getString("image");
                String price = formatPrice(item.getString("lprice"));
                String productURL = item.getString("link");

                ImageView productImage;
                TextView productName;
                TextView productPrice;

                switch (i) {
                    case 0:
                        productImage = findViewById(R.id.productImage1);
                        productName = findViewById(R.id.productName1);
                        productPrice = findViewById(R.id.productPrice1);
                        break;
                    case 1:
                        productImage = findViewById(R.id.productImage2);
                        productName = findViewById(R.id.productName2);
                        productPrice = findViewById(R.id.productPrice2);
                        break;
                    case 2:
                        productImage = findViewById(R.id.productImage3);
                        productName = findViewById(R.id.productName3);
                        productPrice = findViewById(R.id.productPrice3);
                        break;
                    case 3:
                        productImage = findViewById(R.id.productImage4);
                        productName = findViewById(R.id.productName4);
                        productPrice = findViewById(R.id.productPrice4);
                        break;
                    case 4:
                        productImage = findViewById(R.id.productImage5);
                        productName = findViewById(R.id.productName5);
                        productPrice = findViewById(R.id.productPrice5);
                        break;
                    case 5:
                        productImage = findViewById(R.id.productImage6);
                        productName = findViewById(R.id.productName6);
                        productPrice = findViewById(R.id.productPrice6);
                        break;
                    case 6:
                        productImage = findViewById(R.id.productImage7);
                        productName = findViewById(R.id.productName7);
                        productPrice = findViewById(R.id.productPrice7);
                        break;
                    case 7:
                        productImage = findViewById(R.id.productImage8);
                        productName = findViewById(R.id.productName8);
                        productPrice = findViewById(R.id.productPrice8);
                        break;
                    default:
                        continue;
                }

                productName.setText(title);
                productPrice.setText(price);
                Glide.with(this).load(image).into(productImage);

                int finalI = i;
                productImage.setOnClickListener(v -> {
                    try {
                        String link = items.getJSONObject(finalI).getString("link");
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        startActivity(browserIntent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (JSONException e) {
            Log.e("ResearchActivity", "Error displaying products", e);
        }
    }

    //가격 형식 맞추기
    private String formatPrice(String price) {
        int priceValue = Integer.parseInt(price);
        return String.format("%,d원", priceValue);
    }
}
