package com.stacklogic.controller;

import com.stacklogic.model.Note;
import com.stacklogic.util.NoteDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Quick Notes view.
 *
 * Features:
 * - Create, edit, and delete notes
 * - Search notes by text content
 * - Filter notes by tag/category
 * - Custom list cell rendering for note previews
 */
public class NotesController implements Initializable {

    // FXML injected components
    @FXML private TextField searchField;
    @FXML private ComboBox<String> tagFilterCombo;
    @FXML private ListView<Note> notesList;
    @FXML private Label noteCountLabel;

    // Editor components
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> tagCombo;
    @FXML private TextArea noteTextArea;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    // Data
    private ObservableList<Note> allNotes = FXCollections.observableArrayList();
    private Note currentNote = null;
    private boolean isEditing = false;

    // Date formatter
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Set up tag combo with common categories
        tagCombo.setItems(FXCollections.observableArrayList(
            "Preflop", "Postflop", "Reads", "Ranges", "Mindset",
            "Tilt", "Leaks", "Goals", "Review", "Other"
        ));

        // Set up tag filter
        refreshTagFilter();

        // Set default date to today
        datePicker.setValue(LocalDate.now());

        // Set up custom cell factory for notes list
        notesList.setCellFactory(new Callback<ListView<Note>, ListCell<Note>>() {
            @Override
            public ListCell<Note> call(ListView<Note> listView) {
                return new NoteListCell();
            }
        });

        // Listen for note selection
        notesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadNoteIntoEditor(newVal);
            }
        });

        // Listen for search text changes
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterNotes();
        });

        // Listen for tag filter changes
        tagFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterNotes();
        });

        // Initial load
        loadNotes();
    }

    /**
     * Load all notes from the database.
     */
    private void loadNotes() {
        try {
            List<Note> notes = NoteDAO.getAll();
            allNotes.setAll(notes);
            filterNotes();
            refreshTagFilter();
        } catch (Exception e) {
            System.err.println("Error loading notes: " + e.getMessage());
        }
    }

    /**
     * Filter notes based on search text and tag filter.
     */
    private void filterNotes() {
        String searchText = searchField.getText();
        String tagFilter = tagFilterCombo.getValue();

        List<Note> filtered = new ArrayList<>();

        for (Note note : allNotes) {
            // Check tag filter
            if (tagFilter != null && !tagFilter.equals("All Tags") && !tagFilter.isEmpty()) {
                if (note.getTag() == null || !note.getTag().equals(tagFilter)) {
                    continue;
                }
            }

            // Check search text
            if (searchText != null && !searchText.isEmpty()) {
                String lowerSearch = searchText.toLowerCase();
                boolean matches = false;

                if (note.getNoteText() != null && note.getNoteText().toLowerCase().contains(lowerSearch)) {
                    matches = true;
                }
                if (note.getTag() != null && note.getTag().toLowerCase().contains(lowerSearch)) {
                    matches = true;
                }

                if (!matches) {
                    continue;
                }
            }

            filtered.add(note);
        }

        notesList.setItems(FXCollections.observableArrayList(filtered));
        noteCountLabel.setText(filtered.size() + " note" + (filtered.size() != 1 ? "s" : ""));
    }

    /**
     * Refresh the tag filter dropdown with all unique tags.
     */
    private void refreshTagFilter() {
        try {
            List<String> tags = NoteDAO.getAllTags();
            List<String> filterOptions = new ArrayList<>();
            filterOptions.add("All Tags");
            filterOptions.addAll(tags);

            String currentSelection = tagFilterCombo.getValue();
            tagFilterCombo.setItems(FXCollections.observableArrayList(filterOptions));

            if (currentSelection != null && filterOptions.contains(currentSelection)) {
                tagFilterCombo.setValue(currentSelection);
            }
        } catch (Exception e) {
            System.err.println("Error loading tags: " + e.getMessage());
        }
    }

    /**
     * Load a note into the editor.
     */
    private void loadNoteIntoEditor(Note note) {
        currentNote = note;
        isEditing = true;

        datePicker.setValue(note.getDate());
        tagCombo.setValue(note.getTag());
        noteTextArea.setText(note.getNoteText());

        saveButton.setText("Update");
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
    }

    /**
     * Clear the editor for a new note.
     */
    @FXML
    private void handleClear() {
        currentNote = null;
        isEditing = false;

        datePicker.setValue(LocalDate.now());
        tagCombo.setValue(null);
        noteTextArea.clear();

        saveButton.setText("Save");
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);

        notesList.getSelectionModel().clearSelection();
    }

    /**
     * Handle "New Note" button click.
     */
    @FXML
    private void handleNewNote() {
        handleClear();
        noteTextArea.requestFocus();
    }

    /**
     * Handle save button click.
     */
    @FXML
    private void handleSave() {
        // Validate
        if (noteTextArea.getText() == null || noteTextArea.getText().trim().isEmpty()) {
            showAlert("Please enter some note text.");
            return;
        }

        if (datePicker.getValue() == null) {
            datePicker.setValue(LocalDate.now());
        }

        try {
            if (isEditing && currentNote != null) {
                // Update existing note
                currentNote.setDate(datePicker.getValue());
                currentNote.setTag(tagCombo.getValue());
                currentNote.setNoteText(noteTextArea.getText().trim());

                NoteDAO.update(currentNote);
            } else {
                // Create new note
                Note newNote = new Note(
                    datePicker.getValue(),
                    tagCombo.getValue(),
                    noteTextArea.getText().trim()
                );
                NoteDAO.create(newNote);
            }

            // Reload and clear
            loadNotes();
            handleClear();

        } catch (Exception e) {
            showAlert("Error saving note: " + e.getMessage());
        }
    }

    /**
     * Handle delete button click.
     */
    @FXML
    private void handleDelete() {
        if (currentNote == null) return;

        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Note");
        confirm.setHeaderText("Delete this note?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                NoteDAO.delete(currentNote.getId());
                loadNotes();
                handleClear();
            } catch (Exception e) {
                showAlert("Error deleting note: " + e.getMessage());
            }
        }
    }

    /**
     * Show an alert dialog.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Custom ListCell for rendering note previews.
     */
    private class NoteListCell extends ListCell<Note> {
        @Override
        protected void updateItem(Note note, boolean empty) {
            super.updateItem(note, empty);

            if (empty || note == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Build display text
                StringBuilder sb = new StringBuilder();

                // Title/preview
                sb.append(note.getTitle());

                // Tag if present
                if (note.getTag() != null && !note.getTag().isEmpty()) {
                    sb.append("  [").append(note.getTag()).append("]");
                }

                // Date
                sb.append("\n").append(note.getDate().format(DATE_FORMAT));

                setText(sb.toString());

                // Style
                getStyleClass().removeAll("note-item");
                getStyleClass().add("note-item");
            }
        }
    }
}
