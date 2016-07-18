package first.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by dell on 2015/12/21.
 */
public class Search extends Activity {
    TextView searchText;
    Button searchButton;
    Button[] buttons = new Button[12];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        searchText = (TextView)findViewById(R.id.searchID);

        buttons[0] = (Button)findViewById(R.id.button0ID);
        buttons[1] = (Button)findViewById(R.id.button1ID);
        buttons[2] = (Button)findViewById(R.id.button2ID);
        buttons[3] = (Button)findViewById(R.id.button3ID);
        searchButton = (Button)findViewById(R.id.searchButtonID);

        for(int i = 0; i < 4; i++) {
            buttons[i].setOnClickListener(new buttonListener());

        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Search.this, MainActivity.class);
                if (searchText.getText() != null) {
                    intent.putExtra("citynm",searchText.getText().toString());
                }
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }


    class buttonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Button button = (Button)v;
            Intent intent = new Intent(Search.this,MainActivity.class);
            if (searchText.getText() != null) {
                intent.putExtra("citynm",button.getText().toString());
            }
            setResult(RESULT_OK,intent);
            finish();
        }
    }



}
