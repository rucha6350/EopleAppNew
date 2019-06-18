package rucha.sawant.eopleapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import rucha.sawant.eopleapp.Interface.ItemClickListener;
import rucha.sawant.eopleapp.Model.Data;
import rucha.sawant.eopleapp.R;
import rucha.sawant.eopleapp.checksum;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.security.AccessControlContext;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MyViewHolder> {

  private List<Data> listItems;
  private Context context;
  public String id;
  String TAG = "Check Onclick";
  FirebaseAuth mAuth;
  private ItemClickListener itemClickListener;

  public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private TextView type, cost;


    private MyViewHolder(View view) {
      super(view);
      type = view.findViewById(R.id.type);
      cost = view.findViewById(R.id.cost);
      mAuth = FirebaseAuth.getInstance();
      view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      /*Intent intent = new Intent(v.getContext(),EventDetail.class);
      intent.putExtra("id", event_id.getText());
      v.getContext().startActivity(intent);*/
      SharedPreferences preferences = v.getContext().getSharedPreferences("profile", MODE_PRIVATE);
        Intent intent = new Intent(v.getContext(), checksum.class);
        intent.putExtra("orderid", ""+System.currentTimeMillis());
        intent.putExtra("custid", preferences.getString("email","rucha").replace(".",""));
        intent.putExtra("txnamount", cost.getText());
        v.getContext().startActivity(intent);
    }
  }


  public DataAdapter(List<Data> title, Context context) {
    this.listItems = title;
    this.context = context;
  }


  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.data_template, parent, false);

    return new MyViewHolder(itemView);
  }


  @Override
  public void onBindViewHolder(final MyViewHolder holder, int position) {
    final Data list = listItems.get(position);
    holder.type.setText(list.getTypedata());
    holder.cost.setText(list.getCost());
  }


  @Override
  public int getItemCount() {
    return listItems.size();
  }

}