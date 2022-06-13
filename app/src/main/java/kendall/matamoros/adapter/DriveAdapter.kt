package kendall.matamoros.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import kendall.matamoros.R
import kendall.matamoros.entity.File

class DriveAdapter (context: Context, files: List<File>)
    : ArrayAdapter<File>(context, 0, files) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater : LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var rowView = inflater.inflate(R.layout.file_item, parent, false)

        val image = rowView.findViewById<ImageView>(R.id.file_icon)
        val name = rowView.findViewById<TextView>(R.id.file_name)
        val id = rowView.findViewById<TextView>(R.id.file_id)

        val file = getItem(position)

        name.setText(file?.name)
        id.setText("ID: " + file?.idFile)
        image.setImageResource(R.drawable.file)

        return rowView
    }
}