package site.imcu.lcus.Score;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import site.imcu.lcus.R;

/**
 * Created by mengh on 2017/8/9.
 */

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder> {

    private List<Score> mScoreList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView course;
        TextView score;
        TextView sbjg;
        TextView cbjg;

        public ViewHolder(View view){
            super(view);
            course=(TextView)view.findViewById(R.id.course);
            score=(TextView)view.findViewById(R.id.score);
            sbjg=(TextView)view.findViewById(R.id.sbjg);
            cbjg=(TextView)view.findViewById(R.id.cbjg);

        }
    }
    public ScoreAdapter(List<Score> scorelist){
        mScoreList=scorelist;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.score_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return  holder;
    }
    @Override
    public void onBindViewHolder(ViewHolder holder,int position){
        Score score = mScoreList.get(position);
        holder.course.setText(score.getKcm());
        holder.score.setText(score.getCj());
        holder.sbjg.setText("尚不及格"+score.getSbjg().toString());
        holder.cbjg.setText("曾不及格"+score.getCbjg().toString());

    }
    @Override
    public int getItemCount(){
        return mScoreList.size();
    }
}
