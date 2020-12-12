package com.example.trackfield.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.graphing.Graph;
import com.example.trackfield.items.headers.archive.ChartOld;
import com.example.trackfield.items.DistanceItem;
import com.example.trackfield.items.Exerlite;
import com.example.trackfield.items.headers.Goal;
import com.example.trackfield.items.headers.archive.GraphOld;
import com.example.trackfield.items.headers.Header;
import com.example.trackfield.items.IntervalItem;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.items.RouteItem;
import com.example.trackfield.items.headers.Sorter;
import com.example.trackfield.toolbox.Toolbox;
import com.example.trackfield.toolbox.Toolbox.C;
import com.example.trackfield.toolbox.Toolbox.D;
import com.example.trackfield.toolbox.Toolbox.M;
import com.example.trackfield.graphing.GraphView;

import java.time.LocalDate;
import java.util.ArrayList;

public class RecyclerAdapters {

    public static abstract class Base extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int ITEM_SORTER = 0;
        public static final int ITEM_ITEM = 1;
        public static final int ITEM_HEADER_12 = 2;
        public static final int ITEM_HEADER_14 = 3;
        public static final int ITEM_HEADER_18 = 4;
        static final int ITEM_GRAPH_OLD = 5;
        static final int ITEM_CHART_OLD = 6;
        static final int ITEM_CHART_DAILY = 7;
        static final int ITEM_CHART_YEAR = 8;
        static final int ITEM_GOAL = 9;
        static final int ITEM_GRAPH_REC = 10;
        static final int ITEM_GRAPH_MONTH = 11;
        static final int ITEM_GRAPH_WEEK = 12;

        protected Context c;
        protected LayoutInflater inflater;
        protected ItemClickListener listener;

        protected ArrayList<RecyclerItem> itemList;
        protected C.SortMode sortMode = C.SortMode.DATE;
        protected int originId;
        protected LocalDate now;

        ////

        public Base(ArrayList<RecyclerItem> itemList, Context c) {
            this.itemList = itemList;
            this.c = c;
            inflater = LayoutInflater.from(c);
        }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == ITEM_SORTER) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.layout_sorter, parent, false);
                return new SorterVH(cl);
            }
            else if (viewType == ITEM_HEADER_12) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.layout_header_12, parent, false);
                return new HeaderVH(cl);
            }
            else if (viewType == ITEM_HEADER_14) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.layout_header_14, parent, false);
                return new HeaderVH(cl);
            }
            else if (viewType == ITEM_HEADER_18) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.layout_header_18, parent, false);
                return new HeaderVH(cl);
            }
            else if (viewType == ITEM_GOAL) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.layout_item_goal, parent, false);
                return new GoalVH(cl);
            }
            else if (viewType == ITEM_GRAPH_REC) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.graph_rec, parent, false);
                return new GraphRecVH(parent, cl);
            }
            else if (viewType == ITEM_GRAPH_MONTH) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.graph_month, parent, false);
                return new GraphBaseVH(parent, cl);
            }
            else if (viewType == ITEM_GRAPH_WEEK) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.graph_week, parent, false);
                return new GraphBaseVH(parent, cl);
            }

            else throw new IllegalArgumentException();
        }
        @Override public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int pos) {

            RecyclerItem item = getItem(pos);
            boolean visible = item.isVisible();
            //Toolbox.L.setVisibleOrGone(viewHolder.itemView, visible);
            //viewHolder.itemView.setActivated(visible);

            //if (visible) L.animateHeight(viewHolder.itemView, M.px(62));
            //else L.animateHeight(viewHolder.itemView, M.px(0));
            //int height = (int) c.getResources().getDimension(R.dimen.layout_item_exercise);
            //L.animateHeight(viewHolder.itemView, height, visible);
            //if (!expanded) viewHolder.itemView.setLayoutParams(new LinearLayout.LayoutParams(0,0));

            if (viewHolder instanceof ExerciseVH) {

                final ExerciseVH holder = (ExerciseVH) viewHolder;
                final Exerlite e = (Exerlite) getItem(pos);
                final String values = e.printDistance() + C.TAB + e.printTime() + C.TAB + e.printPace();

                holder.primary.setText(e.printPrimary());
                holder.secondary.setText(values);
                holder.caption.setText(e.printCaption());
            }

            else if (viewHolder instanceof DistanceExerciseVH) {

                DistanceExerciseVH holder = (DistanceExerciseVH) viewHolder;
                Exerlite e = (Exerlite) getItem(pos);
                String values = this instanceof DistanceExerciseRA ? e.printDistance() + C.TAB + e.printTimeByDistance(((DistanceExerciseRA) this).distance) + C.TAB + e.printPace() : "";
                String date = e.getDate().format(sortMode == C.SortMode.DATE || e.isYear(LocalDate.now().getYear()) ? C.FORMATTER_REC_NOYEAR : C.FORMATTER_REC);

                holder.primary.setText(date);
                holder.secondary.setText(values);
                holder.caption.setText(e.getRoute());
                holder.originMarker.setVisibility(e.has_id(originId) ? View.VISIBLE : View.GONE);
                holder.recordMarker.setVisibility(e.isTop() ? View.VISIBLE : View.GONE);
                holder.recordMarker.getBackground().setColorFilter(c.getColor(e.isTop(1) ? R.color.colorGold : e.isTop(2) ? R.color.colorSilver : R.color.colorBronze), PorterDuff.Mode.MULTIPLY);
            }
            else if (viewHolder instanceof RouteExerciseVH) {

                RouteExerciseVH holder = (RouteExerciseVH) viewHolder;
                Exerlite e = (Exerlite) getItem(pos);
                String values = e.printDistance() + C.TAB + e.printTime() + C.TAB + e.printPace();
                String date = e.getDate().format(sortMode == C.SortMode.DATE || e.isYear(LocalDate.now().getYear()) ? C.FORMATTER_REC_NOYEAR : C.FORMATTER_REC);

                holder.primary.setText(date);
                holder.secondary.setText(values);
                holder.originMarker.setVisibility(e.has_id(originId) ? View.VISIBLE : View.GONE);
                holder.recordMarker.setVisibility(e.isTop() ? View.VISIBLE : View.GONE);
                holder.recordMarker.getBackground().setColorFilter(c.getColor(e.isTop(1) ? R.color.colorGold : e.isTop(2) ? R.color.colorSilver : R.color.colorBronze), PorterDuff.Mode.MULTIPLY);
            }
            else if (viewHolder instanceof IntervalExerciseVH) {

                IntervalExerciseVH holder = (IntervalExerciseVH) viewHolder;
                Exerlite e = (Exerlite) getItem(pos);
                String values = e.printDistance() + C.TAB + e.printTime() + C.TAB + e.printPace();
                String date = e.getDate().format(sortMode == C.SortMode.DATE || e.isYear(LocalDate.now().getYear()) ? C.FORMATTER_REC_NOYEAR : C.FORMATTER_REC);

                holder.primary.setText(date);
                holder.secondary.setText(values);
                holder.caption.setText(e.getRoute());
                holder.originMarker.setVisibility(e.has_id(originId) ? View.VISIBLE : View.GONE);
                holder.recordMarker.setVisibility(View.GONE);
            }

            else if (viewHolder instanceof DistanceVH) {

                DistanceVH holder = (DistanceVH) viewHolder;
                DistanceItem distance = ((DistanceItem) getItem(pos));

                holder.primary.setText(M.prefix(distance.getDistance(), 2, "m"));
                holder.secondary.setText(distance.printValues());
            }
            else if (viewHolder instanceof RouteVH) {

                final RouteVH holder = (RouteVH) viewHolder;
                final RouteItem routeItem = (RouteItem) getItem(pos);

                holder.primary.setText(routeItem.getName());
                holder.secondary.setText(routeItem.printValues());
            }
            else if (viewHolder instanceof IntervalVH) {

                final IntervalVH holder = (IntervalVH) viewHolder;
                final IntervalItem interval = (IntervalItem) getItem(pos);

                holder.primary.setText(interval.getInterval());
                holder.secondary.setText(interval.printValues());
            }

            else if (viewHolder instanceof HeaderVH) {

                HeaderVH holder = (HeaderVH) viewHolder;
                Header header = (Header) getItem(pos);

                holder.primary.setText(header.getTitle());
                holder.secondary.setText(header.printValues());

                if (!header.isChildrenExpanded()) {
                    int collapsedHeight = header.isType(Header.Type.MONTH) ? (int) c.getResources().getDimension(R.dimen.layout_header_month_collapsed) :
                            (int) c.getResources().getDimension(R.dimen.layout_header_year_collapsed);
                    holder.itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, collapsedHeight));
                }
            }
            else if (viewHolder instanceof RecHeaderVH) {

                final RecHeaderVH holder = ((RecHeaderVH) viewHolder);
                final Header header = (Header) getItem(pos);

                holder.primary.setText(header.getTitle());
            }

            else if (viewHolder instanceof SorterVH) {

                final SorterVH holder = ((SorterVH) viewHolder);
                final Sorter sorter = (Sorter) getItem(pos);

                holder.title.setText(sorter.getTitle());
                sortMode = sorter.getSortMode();
            }
            else if (viewHolder instanceof GraphVH) {

                final GraphVH holder = ((GraphVH) viewHolder);
                final GraphOld graph = (GraphOld) getItem(pos);

                graph.inflateElements(inflater, holder.ll, holder.parent);
            }
            else if (viewHolder instanceof GoalVH) {

                GoalVH holder = ((GoalVH) viewHolder);
                Goal goal = (Goal) getItem(pos);

                //holder.primary.setText("Goal");
                holder.secondary.setText(goal.printValues());
            }
            else if (viewHolder instanceof GraphRecVH) {

                GraphRecVH holder = ((GraphRecVH) viewHolder);
                Graph graph = (Graph) getItem(pos);

                holder.surface.restoreDefaultFocus();
                holder.surface.setGraph(graph);
                holder.low.setText(M.stringTime(graph.getMax(), true));
                holder.high.setText(M.stringTime(graph.getMin(), true));
            }
            else if (viewHolder instanceof GraphBaseVH) {

                GraphBaseVH holder = ((GraphBaseVH) viewHolder);
                Graph graph = (Graph) getItem(pos);

                holder.surface.restoreDefaultFocus();
                holder.surface.setGraph(graph);
            }

            else if (viewHolder instanceof RecyclerAdapters.ExerciseRA.ChartVH) {

                final ChartOld chart = (ChartOld) getItem(pos);
                //float[] y = c.getY();
                String[] xLabel = chart.getxLabel();
                float[] yRel = chart.getyRel();
                int maxHeight = Toolbox.L.px(42);

                for (int i = 0; i < chart.length(); i++) {
                    RecyclerAdapters.ExerciseRA.ChartVH h = (RecyclerAdapters.ExerciseRA.ChartVH) viewHolder;
                    h.weeks[i].setText(xLabel[i]);
                    //h.distances[i].setText(Maths.prefix(y[i], 1) + "m");
                    ViewGroup.LayoutParams params = h.bars[i].getLayoutParams();
                    params.height = (int) (maxHeight * yRel[i]);
                    if (params.height == 0) { params.height = Toolbox.L.px(1); }
                    h.bars[i].setLayoutParams(params);

                }

            }
            else if (viewHolder instanceof RecyclerAdapters.ExerciseRA.DailyChartVH) {

                final ChartOld chart = (ChartOld) getItem(pos);
                float[] yRel = chart.getyRel();
                int maxHeight = Toolbox.L.px(22);

                for (int i = 0; i < chart.length(); i++) {
                    RecyclerAdapters.ExerciseRA.DailyChartVH h = (RecyclerAdapters.ExerciseRA.DailyChartVH) viewHolder;

                    // height
                    ViewGroup.LayoutParams params = h.bars[i].getLayoutParams();
                    params.height = (int) (maxHeight * yRel[i]);
                    if (params.height == 0) { params.height = Toolbox.L.px(1); }
                    h.bars[i].setLayoutParams(params);

                    // color
                    //if (i == now.get(C.DAY_OF_WEEK)-1)      { h.bars[i].setBackgroundColor(c.getResources().getColor(Toolbox.L.getBackgroundResourceFromAttr(R.attr.colorPrimary, c))); }
                    if (i <= now.get(C.DAY_OF_WEEK)-1)  { h.bars[i].setBackgroundColor(c.getResources().getColor(Toolbox.L.getBackgroundResourceFromAttr(R.attr.colorAccent, c))); }
                }

            }
            else if (viewHolder instanceof RecyclerAdapters.ExerciseRA.YearChartVH) {

                final ChartOld chart = (ChartOld) getItem(pos);
                float[] yRel = chart.getyRel();
                int maxHeight = Toolbox.L.px(22); //22
                //char[] labels = chart.getxLabelC();

                RecyclerAdapters.ExerciseRA.YearChartVH h = (RecyclerAdapters.ExerciseRA.YearChartVH) viewHolder;
                h.linearLayout.removeAllViews();

                for (int i = chart.length()-1; i >= 0; i--) {

                    ConstraintLayout element = (ConstraintLayout) inflater.inflate(R.layout.chart_element_bar_small, h.parent, false);
                    h.linearLayout.addView(element);
                    View bar = element.findViewById(R.id.view_barSmall);
                    //((TextView) element.findViewById(R.id.textView_week)).setText(labels[i] + "");

                    // height
                    ViewGroup.LayoutParams params = bar.getLayoutParams();
                    params.height = (int) (maxHeight * yRel[i]);
                    if (params.height == 0) { params.height = Toolbox.L.px(1); }
                    bar.setLayoutParams(params);

                    // color
                    if (i == now.get(C.WEEK_OF_YEAR)-1)      { bar.setBackgroundColor(c.getResources().getColor(Toolbox.L.getBackgroundResourceFromAttr(R.attr.colorPrimary, c))); }
                    else if (i < now.get(C.WEEK_OF_YEAR)-1)  { bar.setBackgroundColor(c.getResources().getColor(Toolbox.L.getBackgroundResourceFromAttr(R.attr.colorAccent, c))); }
                }

            }

        }

        // holders
        public class BaseVH extends RecyclerView.ViewHolder {

            public BaseVH(@NonNull View itemView) {
                super(itemView);
            }

        }
        public class SorterVH extends BaseVH implements View.OnClickListener {

            public ConstraintLayout constraintLayout;
            public ConstraintLayout button;
            public TextView title;

            public SorterVH(ConstraintLayout cl) {
                super(cl);
                constraintLayout = cl;
                button = cl.findViewById(R.id.constraintLayout_sort);
                title = cl.findViewById(R.id.textView_sort);
                cl.setOnClickListener(this);
            }

            @Override public void onClick(View view) {
                if (listener != null) { listener.onItemClick(view, getAdapterPosition(), ITEM_SORTER); }
            }

        }
        public class HeaderVH extends BaseVH implements View.OnClickListener, View.OnLongClickListener {

            public ConstraintLayout constraintLayout;
            public TextView primary;
            public TextView secondary;

            public HeaderVH(ConstraintLayout cl) {
                super(cl);
                constraintLayout = cl;
                primary = cl.findViewById(R.id.textView_primary);
                secondary = cl.findViewById(R.id.textView_secondary);
                cl.setOnClickListener(this);
                cl.setOnLongClickListener(this);
            }

            @Override public void onClick(View view) {
                if (listener != null) { listener.onItemClick(view, getAdapterPosition(), ITEM_HEADER_18); }
            }
            @Override public boolean onLongClick(View view) {
                if (listener != null) { listener.onItemLongClick(view, getAdapterPosition(), ITEM_HEADER_18); }
                return true;
            }

        }
        public class RecHeaderVH extends BaseVH {

            public ConstraintLayout constraintLayout;
            public TextView primary;

            public RecHeaderVH(ConstraintLayout cl) {
                super(cl);
                constraintLayout = cl;
                primary = cl.findViewById(R.id.textView_primary);
            }

        }
        public class GoalVH extends BaseVH {

            public ConstraintLayout constraintLayout;
            public TextView primary;
            public TextView secondary;

            public GoalVH(ConstraintLayout cl) {
                super(cl);
                constraintLayout = cl;
                primary = cl.findViewById(R.id.textView_primary);
                secondary = cl.findViewById(R.id.textView_secondary);
            }

        }

        public class GraphVH extends BaseVH {

            public ViewGroup parent;
            public ConstraintLayout cl;
            public LinearLayout ll;
            //public TextView[] indices = new TextView[graphLength];
            //public ConstraintLayout[] points = new ConstraintLayout[graphLength];

            public GraphVH(ViewGroup parent, ConstraintLayout cl) {
                super(cl);
                this.parent = parent;
                this.cl = cl;
                ll = cl.findViewById(R.id.linearLayout_elementContainer);
            /*for (int i = graphLength-1; i >= 0; i--) {
                linearLayout.addView(elements[i]);
                indices[i] = elements[i].findViewById(R.id.textView_index);
                points[i] = elements[i].findViewById(R.id.constraintLayout_point);
            }*/
            }

        }
        public class GraphRecVH extends BaseVH {

            public ViewGroup parent;
            public ConstraintLayout cl;
            public GraphView surface;
            public TextView low, high;

            public GraphRecVH(ViewGroup parent, ConstraintLayout cl) {
                super(cl);
                this.parent = parent;
                this.cl = cl;
                surface = cl.findViewById(R.id.graphSurface_rec);
                low = cl.findViewById(R.id.textView_low);
                high = cl.findViewById(R.id.textView_high);

                final HorizontalScrollView sv = cl.findViewById(R.id.scrollView_graphSurface);
                sv.post(new Runnable() {
                    @Override public void run() {
                        sv.fullScroll(View.FOCUS_RIGHT);
                        sv.scrollTo(sv.getWidth(), 0);
                    }
                });
            }

        }
        public class GraphBaseVH extends BaseVH {

            public ViewGroup parent;
            public ConstraintLayout cl;
            public GraphView surface;

            public GraphBaseVH(ViewGroup parent, ConstraintLayout cl) {
                super(cl);
                this.parent = parent;
                this.cl = cl;
                surface = cl.findViewById(R.id.graphSurface_base);
            }

        }

        public class ExerciseVH extends BaseVH implements View.OnClickListener {

            public ConstraintLayout constraintLayout;
            public TextView primary;
            public TextView secondary;
            public TextView caption;

            public ExerciseVH(ConstraintLayout cl) {
                super(cl);
                constraintLayout = cl;
                primary = cl.findViewById(R.id.textView_primary);
                secondary = cl.findViewById(R.id.textView_secondary);
                caption = cl.findViewById(R.id.textView_caption);
                cl.setOnClickListener(this);
            }

            @Override public void onClick(View view) {
                if (listener != null) { listener.onItemClick(view, getAdapterPosition(), ITEM_ITEM); }
            }

        }

        public class DistanceExerciseVH extends BaseVH implements View.OnClickListener {

            public ConstraintLayout constraintLayout;
            public TextView primary;
            public TextView secondary;
            public TextView caption;
            public View originMarker;
            public View recordMarker;

            public DistanceExerciseVH(ConstraintLayout cl) {
                super(cl);
                constraintLayout = cl;
                primary = cl.findViewById(R.id.textView_primary);
                secondary = cl.findViewById(R.id.textView_secondary);
                caption = cl.findViewById(R.id.textView_caption);
                originMarker = cl.findViewById(R.id.view_orignMarker);
                recordMarker = cl.findViewById(R.id.view_recordMarker);
                cl.setOnClickListener(this);
            }

            @Override public void onClick(View view) {
                if (listener != null) { listener.onItemClick(view, getAdapterPosition(), ITEM_ITEM); }
            }

        }
        public class RouteExerciseVH extends BaseVH implements View.OnClickListener {

            public ConstraintLayout constraintLayout;
            public TextView primary;
            public TextView secondary;
            public View originMarker;
            public View recordMarker;

            public RouteExerciseVH(ConstraintLayout cl) {
                super(cl);
                constraintLayout = cl;
                primary = cl.findViewById(R.id.textView_primary);
                secondary = cl.findViewById(R.id.textView_secondary);
                originMarker = cl.findViewById(R.id.view_orignMarker);
                recordMarker = cl.findViewById(R.id.view_recordMarker);
                cl.setOnClickListener(this);
            }

            @Override public void onClick(View view) {
                if (listener != null) { listener.onItemClick(view, getAdapterPosition(), ITEM_ITEM); }
            }

        }
        public class IntervalExerciseVH extends BaseVH implements View.OnClickListener {

            public ConstraintLayout constraintLayout;
            public TextView primary;
            public TextView secondary;
            public TextView caption;
            public View originMarker;
            public View recordMarker;

            public IntervalExerciseVH(ConstraintLayout cl) {
                super(cl);
                constraintLayout = cl;
                primary = cl.findViewById(R.id.textView_primary);
                secondary = cl.findViewById(R.id.textView_secondary);
                caption = cl.findViewById(R.id.textView_caption);
                originMarker = cl.findViewById(R.id.view_orignMarker);
                recordMarker = cl.findViewById(R.id.view_recordMarker);
                cl.setOnClickListener(this);
            }

            @Override public void onClick(View view) {
                if (listener != null) { listener.onItemClick(view, getAdapterPosition(), ITEM_ITEM); }
            }

        }

        public class DistanceVH extends BaseVH implements View.OnClickListener {

            public RelativeLayout relativeLayout;
            public TextView primary;
            public TextView secondary;

            public DistanceVH(RelativeLayout rl) {
                super(rl);
                relativeLayout = rl;
                primary = rl.findViewById(R.id.textView_primary);
                secondary = rl.findViewById(R.id.textView_secondary);
                rl.setOnClickListener(this);
            }

            @Override public void onClick(View view) {
                if (listener != null) { listener.onItemClick(view, getAdapterPosition(), ITEM_ITEM); }
            }

        }
        public class RouteVH extends BaseVH implements View.OnClickListener {

            public RelativeLayout relativeLayout;
            public TextView primary;
            public TextView secondary;

            public RouteVH(RelativeLayout rl) {
                super(rl);
                relativeLayout = rl;
                primary = rl.findViewById(R.id.textView_primary);
                secondary = rl.findViewById(R.id.textView_secondary);
                rl.setOnClickListener(this);
            }

            @Override public void onClick(View view) {
                if (listener != null) { listener.onItemClick(view, getAdapterPosition(), ITEM_ITEM); }
            }

        }
        public class IntervalVH extends BaseVH implements View.OnClickListener {

            public RelativeLayout relativeLayout;
            public TextView primary;
            public TextView secondary;

            public IntervalVH(RelativeLayout rl) {
                super(rl);
                relativeLayout = rl;
                primary = rl.findViewById(R.id.textView_primary);
                secondary = rl.findViewById(R.id.textView_secondary);
                rl.setOnClickListener(this);
            }

            @Override public void onClick(View view) {
                if (listener != null) { listener.onItemClick(view, getAdapterPosition(), ITEM_ITEM); }
            }

        }

        // get
        @Override public int getItemViewType(int pos) {

            RecyclerItem item = getItem(pos);

            if (item instanceof Exerlite) {
                return ITEM_ITEM;
            }
            else if (item instanceof RouteItem) {
                return ITEM_ITEM;
            }
            else if (item instanceof DistanceItem) {
                return ITEM_ITEM;
            }
            else if (item instanceof IntervalItem) {
                return ITEM_ITEM;
            }

            else if (item instanceof Header && ((Header) item).isType(Header.Type.MONTH)) {
                return ITEM_HEADER_14;
            }
            else if (item instanceof Header && ((Header) item).isType(Header.Type.YEAR)) {
                return ITEM_HEADER_18;
            }
            else if (item instanceof Header && ((Header) item).isType(Header.Type.REC, Header.Type.WEEK)) {
                return ITEM_HEADER_12;
            }

            else if (item instanceof Sorter) {
                return ITEM_SORTER;
            }
            else if (item instanceof Goal) {
                return ITEM_GOAL;
            }
            else if (item instanceof Graph && item.hasTag(RecyclerItem.TAG_GRAPH_REC)) {
                return ITEM_GRAPH_REC;
            }
            else if (item instanceof Graph && item.hasTag(RecyclerItem.TAG_GRAPH_WEEK)) {
                return ITEM_GRAPH_WEEK;
            }
            else if (item instanceof Graph) {
                return ITEM_GRAPH_MONTH;
            }

            else if (item instanceof GraphOld) {
                return ITEM_GRAPH_OLD;
            }
            else if (item instanceof ChartOld && ((ChartOld) item).isType(ChartOld.TYPE_DAILY)) {
                return ITEM_CHART_DAILY;
            }
            else if (item instanceof ChartOld && ((ChartOld) item).isType(ChartOld.TYPE_YEAR)) {
                return ITEM_CHART_YEAR;
            }
            else if (item instanceof ChartOld) {
                return ITEM_CHART_OLD;
            }

            return -1;
        }
        @Override public int getItemCount() {
            return itemList.size();
        }
        public RecyclerItem getItem(int pos) {
            return itemList.get(pos);
        }

        // click
        public void setClickListener(ItemClickListener itemClickListener) {
            listener = itemClickListener;
        }
        public interface ItemClickListener {
            void onItemClick(View view, int position, int itemType);
            void onItemLongClick(View view, int position, int itemType);
        }

    }

    public static class ExerciseRA extends Base {

        public ExerciseRA(ArrayList<RecyclerItem> itemList, Context c) {
            super(itemList, c);
            now = LocalDate.now();
        }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == ITEM_ITEM) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.layout_item_exercise, parent, false);
                return new ExerciseVH(cl);
            }
            else if (viewType == ITEM_CHART_OLD) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.chart, parent, false);
                ConstraintLayout[] elements = new ConstraintLayout[D.weekAmount];
                for (int i = 0; i < D.weekAmount; i++) {
                    elements[i] = (ConstraintLayout) inflater.inflate(R.layout.chart_element_bar, parent, false);
                }
                return new ChartVH(cl, elements);
            }
            else if (viewType == ITEM_CHART_DAILY) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.chart_daily, parent, false);
                return new DailyChartVH(cl);
            }
            else if (viewType == ITEM_CHART_YEAR) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.chart, parent, false);
                return new YearChartVH(cl, parent);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

        // holders
        public class ChartVH extends RecyclerView.ViewHolder {

            public ConstraintLayout constraintLayout;
            public LinearLayout linearLayout;
            public TextView[] weeks = new TextView[D.weekAmount];
            public TextView[] distances = new TextView[D.weekAmount];
            public View[] bars = new View[D.weekAmount];

            public ChartVH(ConstraintLayout cl, ConstraintLayout[] elements) {
                super(cl);
                constraintLayout = cl;
                linearLayout = cl.findViewById(R.id.linearLayout_elementContainer);
                for (int i = 0; i < D.weekAmount; i++) {
                    linearLayout.addView(elements[i]);
                    weeks[i] = elements[i].findViewById(R.id.textView_week);
                    distances[i] = elements[i].findViewById(R.id.textView_distance);
                    bars[i] = elements[i].findViewById(R.id.view_bar);
                }
            }

        }
        public class DailyChartVH extends RecyclerView.ViewHolder {

            public ConstraintLayout constraintLayout;
            public View[] bars = new View[7];

            public DailyChartVH(ConstraintLayout cl) {
                super(cl);
                constraintLayout = cl;
                bars[0] = cl.findViewById(R.id.view_barMon);
                bars[1] = cl.findViewById(R.id.view_barTue);
                bars[2] = cl.findViewById(R.id.view_barWed);
                bars[3] = cl.findViewById(R.id.view_barThu);
                bars[4] = cl.findViewById(R.id.view_barFri);
                bars[5] = cl.findViewById(R.id.view_barSat);
                bars[6] = cl.findViewById(R.id.view_barSun);
            }
        }
        public class YearChartVH extends RecyclerView.ViewHolder {

            public ConstraintLayout constraintLayout;
            public LinearLayout linearLayout;
            public ViewGroup parent;
            //public View[] bars = new View[D.weekAmount];

            public YearChartVH(ConstraintLayout cl, ViewGroup parent) {
                super(cl);
                constraintLayout = cl;
                linearLayout = cl.findViewById(R.id.linearLayout_elementContainer);
                this.parent = parent;
            }

        }

    }

    public static class DistanceRA extends Base {

        public DistanceRA(ArrayList<RecyclerItem> itemList, Context c) {
            super(itemList, c);
        }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == ITEM_ITEM) {
                RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.layout_item_rec, parent, false);
                return new DistanceVH(rl);
            }
            else if (viewType == ITEM_GRAPH_OLD) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.chart, parent, false);
                return new GraphVH(parent, cl);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

    }
    public static class RouteRA extends Base {

        public RouteRA(ArrayList<RecyclerItem> itemList, Context c) {
            super(itemList, c);
        }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == ITEM_ITEM) {
                RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.layout_item_rec, parent, false);
                return new RouteVH(rl);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

    }
    public static class IntervalRA extends Base {

        public IntervalRA(ArrayList<RecyclerItem> itemList, Context c) {
            super(itemList, c);
        }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == ITEM_ITEM) {
                RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.layout_item_rec, parent, false);
                return new IntervalVH(rl);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

    }

    public static class DistanceExerciseRA extends Base {

        private int distance;

        public DistanceExerciseRA(ArrayList<RecyclerItem> itemList, int distance, int originId, Context c) {
            super(itemList, c);
            this.distance = distance;
            this.originId = originId;
        }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == ITEM_ITEM) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.layout_item_exercise_distance, parent, false);
                return new DistanceExerciseVH(cl);
            }
            else if (viewType == ITEM_GRAPH_OLD) {
                ConstraintLayout cl = GraphOld.inflateLayout(inflater, parent);
                return new GraphVH(parent, cl);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

    }
    public static class RouteExerciseRA extends Base {

        public RouteExerciseRA(ArrayList<RecyclerItem> itemList, int originId, Context c) {
            super(itemList, c);
            this.originId = originId;
        }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == ITEM_ITEM) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.layout_item_exercise_route, parent, false);
                return new RouteExerciseVH(cl);
            }
            else if (viewType == ITEM_GRAPH_OLD) {
                ConstraintLayout cl = GraphOld.inflateLayout(inflater, parent);
            /*ConstraintLayout[] elements = new ConstraintLayout[graphLength];
            for (int i = 0; i < graphLength; i++) {
                elements[i] = (ConstraintLayout) inflater.inflate(R.layout.chart_element_point, parent, false);
            }*/
                return new GraphVH(parent, cl);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

    }
    public static class IntervalExerciseRA extends Base {

        public IntervalExerciseRA(ArrayList<RecyclerItem> itemList, int originId, Context c) {
            super(itemList, c);
            this.originId = originId;
        }
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == ITEM_ITEM) {
                ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.layout_item_exercise_distance, parent, false);
                return new IntervalExerciseVH(cl);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

    }

}
