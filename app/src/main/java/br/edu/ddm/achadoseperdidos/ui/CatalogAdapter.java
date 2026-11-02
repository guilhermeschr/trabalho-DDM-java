package br.edu.ddm.achadoseperdidos.ui;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import br.edu.ddm.achadoseperdidos.R;
import java.util.*;

public final class CatalogAdapter extends RecyclerView.Adapter<CatalogAdapter.Holder> {
    public interface OnClick { void open(String id); }
    public static final class Row {
        final String id, title, subtitle;
        public Row(String id, String title, String subtitle) { this.id = id; this.title = title; this.subtitle = subtitle; }
    }
    private List<Row> rows = Collections.emptyList();
    private final OnClick click;
    public CatalogAdapter(OnClick click) { this.click = click; }
    public void submit(List<Row> rows) { this.rows = new ArrayList<>(rows); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_catalog, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Row row = rows.get(position); holder.title.setText(row.title); holder.subtitle.setText(row.subtitle);
        holder.itemView.setOnClickListener(v -> click.open(row.id));
    }
    @Override public int getItemCount() { return rows.size(); }
    static final class Holder extends RecyclerView.ViewHolder {
        final TextView title, subtitle;
        Holder(View view) { super(view); title = view.findViewById(R.id.row_title); subtitle = view.findViewById(R.id.row_subtitle); }
    }
}
