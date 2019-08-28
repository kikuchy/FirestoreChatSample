package com.diverseinc.firestorechat.ui.talk

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.diverseinc.firestorechat.data.Transcript
import com.diverseinc.firestorechat.data.UserId
import com.diverseinc.firestorechat.databinding.AdapterTalkTranscriptMeBinding
import com.diverseinc.firestorechat.databinding.AdapterTalkTranscriptYouBinding

class TalkListAdapter : RecyclerView.Adapter<TalkListAdapter.ViewHolder>() {
    private var cache: List<Transcript> = emptyList()
    private var fromUserId: UserId = UserId.INVALID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ME) {
            val binding = AdapterTalkTranscriptMeBinding.inflate(inflater, parent, false)
            MeViewHolder(binding)
        } else {
            val binding = AdapterTalkTranscriptYouBinding.inflate(inflater, parent, false)
            YouViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return cache.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cache[position])
    }

    override fun getItemViewType(position: Int): Int {
        return if (cache[position].from == fromUserId) TYPE_ME else TYPE_YOU
    }

    fun update(transcripts: List<Transcript>) {
        cache = transcripts
        notifyDataSetChanged()
    }

    fun update(fromUserId: UserId) {
        this.fromUserId = fromUserId
        notifyDataSetChanged()
    }

    interface Bindable {
        fun bind(transcript: Transcript)
    }

    abstract class ViewHolder(binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root), Bindable

    class MeViewHolder(private val binding: AdapterTalkTranscriptMeBinding) :
        ViewHolder(binding) {
        override fun bind(transcript: Transcript) {
            binding.transcript = transcript
        }
    }

    class YouViewHolder(private val binding: AdapterTalkTranscriptYouBinding) :
        ViewHolder(binding) {
        override fun bind(transcript: Transcript) {
            binding.transcript = transcript
        }
    }

    companion object {
        private const val TYPE_ME = 0
        private const val TYPE_YOU = 1

        @JvmStatic
        @BindingAdapter("transcripts")
        fun RecyclerView.bindTranscripts(transcripts: List<Transcript>?) {
            if (transcripts == null) {
                return
            }
            (adapter as? TalkListAdapter)?.update(transcripts)
        }

        @JvmStatic
        @BindingAdapter("fromUser")
        fun RecyclerView.bindFromUserId(userId: String?) {
            if (userId == null) {
                return
            }
            (adapter as? TalkListAdapter)?.update(UserId(userId))
        }
    }
}

