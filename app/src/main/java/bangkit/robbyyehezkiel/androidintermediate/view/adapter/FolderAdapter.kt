package bangkit.robbyyehezkiel.androidintermediate.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bangkit.robbyyehezkiel.androidintermediate.data.model.FolderResponse
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import bangkit.robbyyehezkiel.androidintermediate.databinding.RvFolderBinding

class FolderAdapter(
    private val data: ArrayList<FolderResponse>
) :
    RecyclerView.Adapter<FolderAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            RvFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(private val binding: RvFolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(folder: FolderResponse) {
            binding.image.setImageBitmap(folder.asset)
            binding.image.setOnClickListener {
                Helper.getImageDownload(folder.path)?.let {
                    Helper.dialogAlertImage(binding.root.context, it, folder.path)
                }
            }
        }
    }
}