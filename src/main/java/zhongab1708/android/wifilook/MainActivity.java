package zhongab1708.android.wifilook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static RecyclerView recyclerview;
    public static MyAdapter<String> adapter;
    private LinearLayoutManager layout;
    private List<String> wifilist;

    private CommonData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            data.result = commandForResult("cat /data/misc/wifi/wpa_supplicant.conf");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Analyse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setview();
    }

    private String commandForResult(String command) throws IOException {

        try {
            Process process = Runtime.getRuntime().exec("su");
            if (process == null) {
                Toast.makeText(this, "Root access failed！", Toast.LENGTH_SHORT).show();

                return "error";
            } else {
                DataOutputStream outputStream = null;
                outputStream = new DataOutputStream(process.getOutputStream());
                outputStream.writeBytes(command + "\n");
                outputStream.flush();
                outputStream.writeBytes("exit\n");
                outputStream.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    total.append(line);
                    total.append("\n");
                }
                return total.toString();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "error";
        }
    }

    private void Analyse() throws IOException {
        String tmp;
        char c = 0;
        boolean nullpsk;
        int i;
        String s = data.result, line;
        ArrayList<String> orgname, orgpasswd;
        orgname = new ArrayList<>();
        orgpasswd = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        while ((line = br.readLine()) != null) {
            if (line.equals("network={")) {
                orgname.add(br.readLine());
                orgpasswd.add(br.readLine());
            }
            data.wifiname = new String[orgname.size()];
            data.wifipasswd = new String[orgpasswd.size()];
            data.getWifiname = new String[orgname.size()];
            data.getWifipasswd = new String[orgpasswd.size()];
            data.allinfo = new String[orgname.size()];
            for (i = 0; i < orgname.size(); i++) {
                data.wifiname[i] = orgname.get(i);
                data.wifipasswd[i] = orgpasswd.get(i);
                data.getWifiname[i] = middle(data.wifiname[i], 7, data.wifiname[i].length());
                tmp = orgpasswd.get(i);
                for (int k = 0; k < 2; k++) {
                    c = tmp.charAt(k);
                }
                if (c == 'k') {
                    data.getWifipasswd[i] = "Empty (Password not exist)";
                    nullpsk = true;
                } else {
                    data.getWifipasswd[i] = middle(data.wifipasswd[i], 6, data.wifipasswd[i].length());
                }
                data.allinfo[i] = data.n + data.getWifiname[i] + "\n" + data.p + data.getWifipasswd[i];
            }
        }
        wifilist = Arrays.asList(data.allinfo);
    }

    public static String middle(String input, int index, int count) {
        if (input.isEmpty()) {
            return "";
        }
        count = (count > input.length() - index + 1) ? input.length() - index + 1 :
                count;
        return input.substring(index - 1, index + count - 1);
    }

    private void setview() {
        recyclerview = (RecyclerView) findViewById(R.id.recycler_view);
        layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerview.setLayoutManager(layout);
        recyclerview.addItemDecoration(new MyDivider(this, MyDivider.VERTICAL_LIST));
        recyclerview.setAdapter(adapter = new MyAdapter<String>(this, wifilist, R.layout.recycler_view) {
            @Override
            public void convert(MyHolder holder, String data, int position) {
                holder.setText(R.id.wifinfo, data);
                holder.setImageResource(R.id.image, R.mipmap.ic_launcher);
            }
        });
    }
}
