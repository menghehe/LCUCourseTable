package site.imcu.lcus.score;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import site.imcu.lcus.R;

/**
 * Created by SHIELD_7 on 2017/8/9
 */

 class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder> {

    private List<Score> mScoreList;

    private OnItemClickListener mOnItemClickListener;

    interface OnItemClickListener
    {
        void onItemClick(View view, int position);

    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener)
    {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView courseNameView;
        TextView creditView;
        TextView courseAttrView;
        TextView markView;
        TextView positionView;
        CardView cardView;


        private ViewHolder(View view){
            super(view);
            courseNameView=(TextView)view.findViewById(R.id.item_name);
            creditView=(TextView)view.findViewById(R.id.item_credit);
            courseAttrView=(TextView)view.findViewById(R.id.item_attr);
            markView=(TextView)view.findViewById(R.id.item_mark);
            positionView=(TextView)view.findViewById(R.id.item_position);
            cardView=(CardView)view.findViewById(R.id.card_score_view);
            }

    }
    ScoreAdapter(List<Score> scoreList){
        mScoreList=scoreList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.score_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position){
        Score score = mScoreList.get(position);
        holder.courseNameView.setText(score.getCourseName());
        holder.creditView.setText(score.getCredit());
        holder.courseAttrView.setText(score.getCourseAttr());
        holder.markView.setText(score.getMark());
        holder.positionView.setText(score.getPosition());

        if (mOnItemClickListener!=null) {

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.cardView,pos);
                }
            });
        }
        }
    @Override
    public int getItemCount(){
        return mScoreList.size();
    }

}


