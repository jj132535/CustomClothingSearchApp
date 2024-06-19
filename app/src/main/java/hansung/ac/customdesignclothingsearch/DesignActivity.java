package hansung.ac.customdesignclothingsearch;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DesignActivity extends AppCompatActivity {

    // 네이버 API 인증키
    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String CLIENT_SECRET = "CLIENT_SECRET";

    private Spinner clothesTypeSpinner;
    private ImageView silhouetteImage;
    private LinearLayout colorLinearLayout;
    private GridLayout partsGridLayout;
    private Button completeButton;

    // 옷 종류, 이미지 매핑
    private String[] clothesTypes = {"반팔티셔츠", "긴팔티셔츠", "와이셔츠", "긴바지", "반바지", "원피스"};
    private int[] clothesImages = {R.drawable.clothes_short_t_shirt, R.drawable.clothes_long_t_shirt, R.drawable.clothes_y_shirt, R.drawable.clothes_long_pants, R.drawable.clothes_short_pants, R.drawable.clothes_dress};

    // 색상 이미지, 이름 매핑
    private int[] colorImageViewIds = {R.id.colorImage1, R.id.colorImage2, R.id.colorImage3, R.id.colorImage4, R.id.colorImage5, R.id.colorImage6, R.id.colorImage7, R.id.colorImage8, R.id.colorImage9};
    private String[] colorNames = {"검정", "청색", "베이지", "회색", "녹색", "아이보리", "민트", "빨강", "흰색"};

    // 파츠 이미지, 이름 매핑
    private int[] partsImageViewIds = {R.id.partsImage1, R.id.partsImage2, R.id.partsImage3, R.id.partsImage4, R.id.partsImage5, R.id.partsImage6, R.id.partsImage7};
    private String[] partsNames = {"라운드카라", "레귤러카라", "와이드카라", "오픈카라", "단추", "포켓", "카고"};
    private int[] partsImageResources = {R.drawable.parts_crop_collar1, R.drawable.parts_crop_collar2, R.drawable.parts_crop_collar3, R.drawable.parts_crop_collar4, R.drawable.parts_crop_button1, R.drawable.parts_crop_pocket1, R.drawable.parts_crop_cargo1};
    private int[] partsPreviewImage = {R.drawable.parts_collar1, R.drawable.parts_collar2, R.drawable.parts_collar3, R.drawable.parts_collar4, R.drawable.parts_button1, R.drawable.parts_pocket1, R.drawable.parts_cargo1};

    // 선택된 키워드 정리
    private String selectedClothesType, selectedColorType, requestPromptTxt;
    private List<String> selectedPartsTypes = new ArrayList<>();

    // 요소별 선택한 개수 카운트(다중 선택 방지)
    private int colorCount = 0;
    private int partsCount = 0;

    private ImageView[] partsPreviewImages = new ImageView[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design);

        clothesTypeSpinner = findViewById(R.id.clothesTypeSpinner);
        silhouetteImage = findViewById(R.id.silhouetteImage);
        colorLinearLayout = findViewById(R.id.colorLinerLayout);
        partsGridLayout = findViewById(R.id.partsGridLayout);
        completeButton = findViewById(R.id.completeButton);

        partsPreviewImages[0] = findViewById(R.id.partsPreview1);
        partsPreviewImages[1] = findViewById(R.id.partsPreview2);
        partsPreviewImages[2] = findViewById(R.id.partsPreview3);

        // 스피너 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clothesTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clothesTypeSpinner.setAdapter(adapter);

        clothesTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSilhouetteImage(position);
                selectedClothesType = clothesTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setupColorSelection();
        setupPartsSelection();

        // 상품 검색하기 버튼 클릭
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPromptTxt = selectedPartsTypes + " " + selectedColorType + " " + selectedClothesType;
                new FetchProductsTask().execute(requestPromptTxt);
            }
        });
    }

    // 선택된 옷의 실루엣(템플릿) 이미지 갱신
    private void updateSilhouetteImage(int position) {
        silhouetteImage.setImageResource(clothesImages[position]);
    }

    // 색상 선택 초기화
    private void setupColorSelection() {
        for (int i = 0; i < colorImageViewIds.length; i++) {
            ImageView colorImageView = findViewById(colorImageViewIds[i]);
            int finalI = i;
            colorImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectColor(finalI);
                }
            });
        }
    }

    // 색상 선택 시 동작
    private void selectColor(int index) {
        if (colorCount > 0) {
            for (int id : colorImageViewIds) {
                findViewById(id).setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
        }
        findViewById(colorImageViewIds[index]).setBackgroundColor(getResources().getColor(android.R.color.black));
        selectedColorType = colorNames[index];
        colorCount = 1;
    }

    // 파츠 선택 초기화
    private void setupPartsSelection() {
        for (int i = 0; i < partsImageViewIds.length; i++) {
            ImageView partsImageView = findViewById(partsImageViewIds[i]);
            int finalI = i;
            partsImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePartSelection(finalI, partsImageView);
                }
            });
        }
    }

    // 파츠 선택,해제 시 동작
    private void togglePartSelection(int index, ImageView partsImageView) {
        String part = partsNames[index];
        if (selectedPartsTypes.contains(part)) {
            selectedPartsTypes.remove(part);
            partsCount--;
            partsImageView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            updatePartsPreviewImages();
        } else {
            if (partsCount < 3) {
                selectedPartsTypes.add(part);
                partsCount++;
                partsImageView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                updatePartsPreviewImages();
            }
        }
    }

    // 선택된 파츠 프리뷰 이미지 업데이트
    private void updatePartsPreviewImages() {
        for (int i = 0; i < partsPreviewImages.length; i++) {
            if (i < selectedPartsTypes.size()) {
                int partIndex = getPartIndex(selectedPartsTypes.get(i));
                partsPreviewImages[i].setImageResource(partsPreviewImage[partIndex]);
            } else {
                partsPreviewImages[i].setImageResource(android.R.color.transparent);
            }
        }
    }

    // 파츠 이름으로 인덱스 찾기
    private int getPartIndex(String partName) {
        for (int i = 0; i < partsNames.length; i++) {
            if (partsNames[i].equals(partName)) {
                return i;
            }
        }
        return -1; // should not happen
    }

    // API 호출 클래스
    private class FetchProductsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String query = params[0];
            try {
                String apiURL = "https://openapi.naver.com/v1/search/shop.json?query=" + query + "&display=15";
                URL url = new URL(apiURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-Naver-Client-Id", CLIENT_ID);
                connection.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);

                int responseCode = connection.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else { // 에러 발생
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                return response.toString();
            } catch (Exception e) {
                Log.e("FetchProductsTask", "Error fetching product data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray items = jsonObject.getJSONArray("items");
                    Intent intent = new Intent(DesignActivity.this, ResearchActivity.class);
                    intent.putExtra("items", items.toString());
                    startActivity(intent);
                } catch (JSONException e) {
                    Log.e("FetchProductsTask", "Error parsing JSON data", e);
                    Toast.makeText(DesignActivity.this, "데이터 처리중 오류.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DesignActivity.this, "API 호출 실패.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
