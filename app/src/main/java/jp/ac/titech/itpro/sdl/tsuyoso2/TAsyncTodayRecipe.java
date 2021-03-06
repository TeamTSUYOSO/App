package jp.ac.titech.itpro.sdl.tsuyoso2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Yamada on 2016/10/17.
 */
public class TAsyncTodayRecipe extends AsyncTask<String, Integer, JSONObject> {

    private Activity fActivity;
    private ProgressDialog progressDialog;

    int fRequestId;

    TextView fRecipe_name;
    TextView fServing_num;
    TextView fCooking_time;
    TextView fGenre;
    TextView fCalorie;
    TextView fPrice;
    LinearLayout fInstructions;
    ArrayList<String> fInstructionList;
    LinearLayout fIngredients;
    ArrayList<String> fIngredientList;
    ImageView fImageView;

    /**
     * コンストラクタ
     * @param activity
     */
    public TAsyncTodayRecipe(Activity activity,int requestId, TextView recipe_name, TextView serving_num, TextView cooking_time, TextView genre, TextView calorie,
                             TextView price, LinearLayout instructions,  ArrayList<String> instructionList,
                             LinearLayout ingredients, ArrayList<String> ingredientList, ImageView imageView){
        this.fActivity = activity;
        fRequestId = requestId;
        fRecipe_name = recipe_name;
        fCooking_time = cooking_time;
        fServing_num = serving_num;
        fGenre = genre;
        fCalorie = calorie;
        fPrice = price;
        fInstructions = instructions;
        fInstructionList = instructionList;
        fIngredients = ingredients;
        fIngredientList = ingredientList;
        fImageView = imageView;

    }

    /**
     * doInBackgroundの前処理
     */
    @Override
    protected void onPreExecute() {
        /* TODO
         * doInBackGroundの前処理.
         */

        System.out.println("onPreExecute");
        super.onPreExecute();
        // doInBackground前処理

        progressDialog = new ProgressDialog(fActivity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    /**
     * バックグラウンドで実行する処理
     * @param params
     * @return
     */
    @Override
    protected JSONObject doInBackground(String... params) {
        /* TODO
         * Activityから受け渡されるデータをサーバに送って帰ってきたデータをリターンする
         * onPostExecuteに渡される.
         */
        System.out.println("doInBackGround");

        HttpURLConnection connection = null;
        URL url = null;

        /* TODO
         * fRequestIdはローカルDBから受け取ったものを送る
         */

        String urlString = "http://160.16.213.209:8080/api/recipe/" + fRequestId;
        String readData = "";

        try{
            //URL生成
            url = new URL(urlString);
            //接続用HttpURLConnection生成
            connection = (HttpURLConnection)url.openConnection();
            //リクエストメソッドの設定
            //POSTするときはここをPOST
            connection.setRequestMethod("GET");
            //リダイレクトを自動で許可しない設定
            connection.setInstanceFollowRedirects(false);
            //URL接続からデータを読み取るにはtrue
            connection.setDoInput(true);
            // URL接続にデータを書き込む場合はtrue
            connection.setDoOutput(false);

            // 接続
            connection.connect();

            publishProgress();

            final int status = connection.getResponseCode();
            if(status == HttpURLConnection.HTTP_OK){
                System.out.println("connection OK");
                readData = readInputStream(connection);
            }

        }
        catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            return new JSONObject(readData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 実行中
     * @param progress
     */
    public void progressUpdate(Integer... progress) {
        progressDialog.incrementProgressBy(progress[0]);
    }

    /**
     * DoInBackgroundの後処理
     * @param jsonObject
     */
    @Override
    protected void onPostExecute(JSONObject jsonObject){

        System.out.println("onPostExecute");
//        System.out.println(jsonObject.toString());
        // doInBackground後処理

        if(jsonObject != null) {
            try {

                fRecipe_name.setText(jsonObject.getString("name"));
                fCooking_time.setText(jsonObject.getString("takes_time"));
                fServing_num.setText(jsonObject.getString("serving_num"));

                fGenre.setText(jsonObject.getString("foods_genre"));
                fCalorie.setText(jsonObject.getString("calorie"));
                fPrice.setText(jsonObject.getString("price"));

                JSONArray ingredientsJsonArray = jsonObject.getJSONArray("ingredients");
                JSONArray instructionsJsonArray = jsonObject.getJSONArray("instructions");

                for (int i = 0; i < ingredientsJsonArray.length(); i++) {
                    JSONObject ingredient = ingredientsJsonArray.getJSONObject(i);

                    View view = fActivity.getLayoutInflater().inflate(R.layout.row_ingredients, null);
                    fIngredients.addView(view);
                    TextView name = (TextView) view.findViewById(R.id.ingredient_name);
                    name.setText(ingredient.getString("name"));
                    TextView quantity = (TextView) view.findViewById(R.id.ingredient_quantity);
                    quantity.setText(ingredient.getString("quantity"));
                }

                for (int i = 0; i < instructionsJsonArray.length(); i++) {
                    JSONObject instruction = instructionsJsonArray.getJSONObject(i);

                    View view = fActivity.getLayoutInflater().inflate(R.layout.row_instructions, null);
                    fInstructions.addView(view);
                    TextView order = (TextView) view.findViewById(R.id.instruction_order);
                    order.setText(instruction.getString("order"));
                    TextView content = (TextView) view.findViewById(R.id.instruction_content);
                    content.setText(instruction.getString("content"));
                }
                

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(fActivity, "サーバーと通信できません.", Toast.LENGTH_SHORT).show();
        }
        //ダイアログ消す
        progressDialog.dismiss();

    }

    /**
     * サーバーから帰ってきたデータの読み込み
     * @param connection
     * @return
     * @throws IOException
     */
    public String readInputStream(HttpURLConnection connection) throws IOException {

        InputStream inputStream = connection.getInputStream();
        StringBuffer stringBuffer = new StringBuffer();
        String string;

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        while ((string = br.readLine()) != null) {
            stringBuffer.append(string);
        }

        try {
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stringBuffer.toString();

    }
}
