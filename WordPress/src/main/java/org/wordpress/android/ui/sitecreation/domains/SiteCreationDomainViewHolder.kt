package org.wordpress.android.ui.sitecreation.domains

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.wordpress.android.R
import org.wordpress.android.ui.sitecreation.domains.SiteCreationDomainsViewModel.DomainsListItemUiState
import org.wordpress.android.ui.sitecreation.domains.SiteCreationDomainsViewModel.DomainsListItemUiState.DomainsFetchSuggestionsErrorUiState
import org.wordpress.android.ui.sitecreation.domains.SiteCreationDomainsViewModel.DomainsListItemUiState.DomainsModelUiState

sealed class SiteCreationDomainViewHolder(internal val parent: ViewGroup, @LayoutRes layout: Int) :
        RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false)) {
    abstract fun onBind(uiState: DomainsListItemUiState)

    class DomainSuggestionItemViewHolder(
        parentView: ViewGroup
    ) : SiteCreationDomainViewHolder(parentView, R.layout.site_creation_domains_item) {
        private val container = itemView.findViewById<ViewGroup>(R.id.container)
        private val suggestion = itemView.findViewById<RadioButton>(R.id.domain_suggestion)
        private var onDomainSelected: (() -> Unit)? = null

        init {
            suggestion.buttonTintList = ContextCompat.getColorStateList(
                    parentView.context,
                    R.color.neutral_100_primary_400_selector
            )
            container.setOnClickListener {
                onDomainSelected?.invoke()
            }
        }

        override fun onBind(uiState: DomainsListItemUiState) {
            uiState as DomainsModelUiState
            onDomainSelected = requireNotNull(uiState.onItemTapped) { "OnItemTapped is required." }
            suggestion.text = uiState.name
            suggestion.isChecked = uiState.checked
        }
    }

    class DomainSuggestionErrorViewHolder(
        parentView: ViewGroup
    ) : SiteCreationDomainViewHolder(parentView, R.layout.site_creation_suggestions_error_item) {
        private val text = itemView.findViewById<TextView>(R.id.error_text)
        private val retry = itemView.findViewById<TextView>(R.id.retry)

        init {
            addRetryCompoundDrawable()
        }

        private fun addRetryCompoundDrawable() {
            val drawable = itemView.context.getDrawable(R.drawable.retry_icon)
            drawable.setTint(ContextCompat.getColor(itemView.context, R.color.primary))
            retry.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

        override fun onBind(uiState: DomainsListItemUiState) {
            uiState as DomainsFetchSuggestionsErrorUiState
            text.text = itemView.context.getText(uiState.messageResId)
            retry.text = itemView.context.getText(uiState.retryButtonResId)
            requireNotNull(uiState.onItemTapped) { "OnItemTapped is required." }
            itemView.setOnClickListener {
                uiState.onItemTapped!!.invoke()
            }
        }
    }
}
