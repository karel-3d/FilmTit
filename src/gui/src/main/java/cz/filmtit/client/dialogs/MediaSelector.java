package cz.filmtit.client.dialogs;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import cz.filmtit.client.callables.CreateDocument;
import cz.filmtit.share.MediaSource;



public class MediaSelector extends Dialog {
    private List<MediaSource> suggestions;
    private MediaSource selected;


    private static MediaSelectorUiBinder uiBinder = GWT.create(MediaSelectorUiBinder.class);

    interface MediaSelectorUiBinder extends UiBinder<Widget, MediaSelector> {
    }

    static class MediaCell extends AbstractCell<MediaSource> {

        @Override
        public void render(Cell.Context context, MediaSource value, SafeHtmlBuilder sb) {
            // Value can be null, so do a null check..
            if (value == null) {
                return;
            }

            sb.appendHtmlConstant("<table>");

            // Add the contact image.
            sb.appendHtmlConstant("<tr><td rowspan='3' class='mediasource_thumb'>");

            if (value.getThumbnailURL() != null)
                sb.appendHtmlConstant("<img src='" + value.getThumbnailURL() + "' />");
            else
                sb.appendHtmlConstant("<div class='no_thumb'>?</div>");

            sb.appendHtmlConstant("</td>");

            // Add the name and address.
            sb.appendHtmlConstant("<td style='font-size:95%;'>");
            sb.appendEscaped(value.toString());
            sb.appendHtmlConstant("</td></tr><tr><td>");
            sb.appendEscaped("");
            sb.appendHtmlConstant("</td></tr></table>");
        }
    }

    /**
     * To be called on submit.
     */
    CreateDocument createDocument;
    
    public MediaSelector(List<MediaSource> suggestions, CreateDocument createDocument) {
        listbox = new CellList<MediaSource>(new MediaCell());

        initWidget(uiBinder.createAndBindUi(this));
        
        listbox.addStyleName("mediasource_selector");
        listbox.setRowData(0, suggestions);
        listbox.setRowCount(suggestions.size());

        final SingleSelectionModel<MediaSource> selectionModel = new SingleSelectionModel<MediaSource>();
        listbox.setSelectionModel(selectionModel);

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                selected = selectionModel.getSelectedObject();
            }
        });
        
        this.suggestions = suggestions;
        this.createDocument = createDocument;

        // TODO: to be added
        // dialogBox.setPopupPosition(dialogBox.getPopupLeft(), 100);
        
        dialogBox.show();
    }

//    public void setSelected(MediaSource mediaSource) {
//        this.selected = mediaSource;
//    }

    @UiField(provided = true)
    CellList<MediaSource> listbox;

    @UiField
    Button submitButton;
    
    @UiHandler("submitButton")
    void submitButtonClicked(ClickEvent e) {
    	close();
    	createDocument.selectSource(getSelected());
    }
    
    @Override
    protected void onHide() {
    	createDocument.selectSource(null);
    }

    private MediaSource getSelected() {
        return selected;
    }

}