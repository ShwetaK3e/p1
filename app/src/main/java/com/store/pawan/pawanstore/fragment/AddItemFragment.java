package com.store.pawan.pawanstore.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.store.pawan.pawanstore.Adapter.FinaltemAdapter;
import com.store.pawan.pawanstore.Adapter.ItemAdapter;
import com.store.pawan.pawanstore.CustomWidgets.PStoreEditTextBold;
import com.store.pawan.pawanstore.CustomWidgets.PStoreTextViewItalic;
import com.store.pawan.pawanstore.R;
import com.store.pawan.pawanstore.model.EntryItem;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;


import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;


public class AddItemFragment extends Fragment {



    ImageButton new_list;
    ImageButton show_bill;

    PStoreTextViewItalic no_item_text;
    RecyclerView item_list;
    ItemAdapter itemAdapter;


    ImageButton add_item;

    //Add Dialog
    Dialog add_bill_item_dialog;

    //Final List
    Dialog final_list_dialog;


    public static List<EntryItem> items=new LinkedList<>();
    private PublishSubject<Integer> listCount;

    public static AddItemFragment newInstance() {
        AddItemFragment fragment = new AddItemFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_add_item, container, false);
        no_item_text= view.findViewById(R.id.no_item_text);
        listCount= PublishSubject.create();
        listCount.subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {
                if(integer!=0){
                    no_item_text.setVisibility(View.INVISIBLE);
                }else{
                    no_item_text.setVisibility(View.VISIBLE);
                }
            }
        });
        listCount.onNext(items.size());

        new_list=view.findViewById(R.id.refresh);
        new_list.setOnClickListener(click->{
            if(items!=null && !items.isEmpty()){
                items.clear();
                listCount.onNext(items.size());
                if(itemAdapter!=null){
                    itemAdapter.notifyDataSetChanged();
                }
            }
        });

        show_bill = view.findViewById(R.id.show_bill);
        show_bill.setOnClickListener(aView-> {
            if(final_list_dialog==null) {
                showFinalList();
            }
        });
        item_list= view.findViewById(R.id.item_list);
        add_item= view.findViewById(R.id.add_item);
        add_item.setOnClickListener(view1 -> {
            if(add_bill_item_dialog==null) {
                showAddItemDialog(new EntryItem());
            }


        });


        add_item.setOnClickListener(view12 -> showAddItemDialog(new EntryItem()));

        item_list.setLayoutManager(new GridLayoutManager(getContext(),1));

        itemAdapter=new ItemAdapter(getContext(), items, pos1 -> {
            if(add_bill_item_dialog==null){
                showAddItemDialog(items.get(pos1));
            }
        });
        item_list.setAdapter(itemAdapter);
        return  view;
    }






    boolean select_instrument_show=true;
    int item_no=0;


    void showAddItemDialog(EntryItem item){
        add_bill_item_dialog=new Dialog(getActivity(),R.style.MyDialogTheme);
        add_bill_item_dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
        add_bill_item_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        add_bill_item_dialog.setContentView(R.layout.bill_entry_dialog);
        item_no=items.size();


        final EntryHolder holder=new EntryHolder(add_bill_item_dialog);
        Observable<CharSequence> qnty_obv= RxTextView.textChanges(holder.sell_count);
        Observable<CharSequence> price_obv= RxTextView.textChanges(holder.price);
        Observable<CharSequence> tax_obv= RxTextView.textChanges(holder.gst);

        Observable.combineLatest( qnty_obv, price_obv, tax_obv, ( charSequence, charSequence2, charSequence3) -> {
            if(charSequence.length()!=0 && !charSequence.toString().equals("0") && charSequence2.length()!=0 &&  charSequence3.length()!=0){
                return true;
            }
            return false;
        }).subscribe(aBoolean -> {
            holder.add.setEnabled(aBoolean);
            holder.add.setTextColor(aBoolean?getResources().getColor(R.color.colorPrimary):getResources().getColor(R.color.light_grey));
        });


        qnty_obv.debounce(2000, TimeUnit.MILLISECONDS)
                .subscribe(text->{
                    item.setQty(Integer.parseInt(holder.sell_count.getText().toString().length()==0?"0":holder.sell_count.getText().toString()));
                });
        price_obv.debounce(2000, TimeUnit.MILLISECONDS)
                .subscribe(text->{
                    item.setPrice(Double.parseDouble(holder.price.getText().toString().length()==0?"0":holder.price.getText().toString()));
                });
        tax_obv.debounce(2000, TimeUnit.MILLISECONDS)
                .subscribe(text->{
                    item.setTax(Double.parseDouble(holder.gst.getText().toString().length()==0?"0":holder.gst.getText().toString()));
                });

        if(item.getQty()!=0) {
            select_instrument_show=false;

           // holder.item_list_layout.animate().translationY(600).setDuration(300);
           // holder.item_name.setText(item.getItemName());
            holder.price.setText(String.valueOf(item.getPrice()/item.getQty()));
            holder.gst.setText(String.valueOf(item.getTax()));
            holder.sell_count.setText(String.valueOf(item.getQty()));
            items.remove(item);
            listCount.onNext(items.size());
        }

        holder.inc_sell_count.setOnClickListener(view -> {
            String qty= holder.sell_count.getText().toString();
            if(qty.length()==0){
                qty="0";
            }
           holder.sell_count.setText(String.valueOf(Integer.parseInt(qty)+1));

        });

        holder.dec_sell_count.setOnClickListener(view -> {
            String qty= holder.sell_count.getText().toString();
            if(qty.length()==0){
                qty="0";
            }
            holder.sell_count.setText(String.valueOf(Integer.parseInt(qty)-1));
        });

        holder.inc_sell_count_layout.setOnClickListener(view -> {
            String qty= holder.sell_count.getText().toString();
            if(qty.length()==0){
                qty="0";
            }
            holder.sell_count.setText(String.valueOf(Integer.parseInt(qty)+1));

        });

        holder.dec_sell_count_layout.setOnClickListener(view -> {
            String qty= holder.sell_count.getText().toString();
            if(qty.length()==0){
                qty="0";
            }
            holder.sell_count.setText(String.valueOf(Integer.parseInt(qty)-1));
        });




       holder.cancel.setOnClickListener(view -> {
           add_bill_item_dialog.dismiss();

               }
       );

       holder.add.setOnClickListener(view -> {
           add_bill_item_dialog.dismiss();
           item.setQty(Integer.parseInt(holder.sell_count.getText().toString().trim()));
           item.setPrice(item.getPrice()*item.getQty());
           items.add(item);
           listCount.onNext(items.size());
           itemAdapter.notifyDataSetChanged();

       });


       add_bill_item_dialog.setOnDismissListener(view->{
           add_bill_item_dialog=null;
           if(item_no>items.size()) {
               items.add(item);
               listCount.onNext(items.size());
               itemAdapter.notifyDataSetChanged();
           }
       });
       add_bill_item_dialog.show();
       add_bill_item_dialog.setCancelable(true);
    }




    class EntryHolder{

        //ImageButton item_img;
        //TextView item_name;
        LinearLayout inc_sell_count_layout;
        LinearLayout dec_sell_count_layout;
        ImageButton inc_sell_count;
        ImageButton dec_sell_count;
        EditText sell_count;
        EditText price;
        EditText gst;
        Button add;
        Button cancel;
       // LinearLayout item_list_layout;
       // RecyclerView instrument_list;


        EntryHolder(Dialog dialog){

            //item_img=(ImageButton)dialog.findViewById(R.id.item_type_img);
            //item_name=(PStoreTextViewBold)dialog.findViewById(R.id.item_type_name);

            inc_sell_count_layout=dialog.findViewById(R.id.inc_sell_count_layout);
            dec_sell_count_layout=dialog.findViewById(R.id.dec_sell_count_layout);
            inc_sell_count=(ImageButton)dialog.findViewById(R.id.inc_sell_count);
            dec_sell_count=(ImageButton)dialog.findViewById(R.id.dec_sell_count);
            sell_count=(PStoreEditTextBold) dialog.findViewById(R.id.sell_count);

            price=(PStoreEditTextBold)dialog.findViewById(R.id.item_price);
            gst=(PStoreEditTextBold)dialog.findViewById(R.id.item_gst);

            add=(Button)dialog.findViewById(R.id.add);
            add.setEnabled(false);
            cancel=(Button)dialog.findViewById(R.id.cancel);

        }

    }


    void showFinalList(){
        final_list_dialog=new Dialog(getActivity(),R.style.MyDialogTheme);
        final_list_dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation1;
        final_list_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final_list_dialog.setContentView(R.layout.final_bill_dialog);

        final  FinalListHolder holder=new FinalListHolder(final_list_dialog);
        FinaltemAdapter finaltemAdapter=new FinaltemAdapter(getContext(),items);
        holder.final_list.setLayoutManager(new GridLayoutManager(getContext(),1));
        holder.final_list.setAdapter(finaltemAdapter);

        double tot_cgst=0.0f;
        double tot_price=0.0f;
        double tot_original_price=0.0f;
        double percentage=0.0f;
        for(EntryItem item: items){
            percentage=item.getTax()/100;
            tot_price+=Math.round(item.getPrice());
            tot_original_price+=Math.round(item.getPrice()-((percentage*item.getPrice())*100.0)/100.0);
            tot_cgst=Math.ceil((tot_price-tot_original_price)/2);
        }


        holder.total_price.setText("Rs. "+String.valueOf(tot_original_price+tot_cgst+tot_cgst));
        holder.total_original_price.setText(String.valueOf(tot_original_price));
        holder.total_cgst.setText(String.valueOf(tot_cgst));
        holder.total_sgst.setText(String.valueOf(tot_cgst));


        final_list_dialog.setOnDismissListener(view->{
            final_list_dialog=null;
        });
        final_list_dialog.show();
        final_list_dialog.setCancelable(true);
    }

    class FinalListHolder{

        RecyclerView final_list;
        TextView total_price;
        TextView total_original_price;
        TextView total_cgst;
        TextView total_sgst;
        FinalListHolder(Dialog dialog){
            final_list= dialog.findViewById(R.id.final_list);
            total_price=dialog.findViewById(R.id.total_price);
            total_original_price=dialog.findViewById(R.id.total_original);
            total_cgst=dialog.findViewById(R.id.total_cgst);
            total_sgst=dialog.findViewById(R.id.total_sgst);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("GENERATE BILL");
    }


}
