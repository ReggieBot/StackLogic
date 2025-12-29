package com.stacklogic.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Strategy reference view.
 *
 * Manages the accordion sections for poker strategy content.
 * Mostly static content - controller handles optional state like
 * remembering which sections were expanded.
 */
public class StrategyController implements Initializable {

    @FXML private Accordion microAccordion;
    @FXML private Accordion preflopAccordion;
    @FXML private Accordion postflopAccordion;
    @FXML private Accordion exploitAccordion;
    @FXML private Accordion handTypesAccordion;
    @FXML private Accordion boardAccordion;
    @FXML private Accordion streetAccordion;
    @FXML private Accordion intermediateAccordion;
    @FXML private Accordion advancedAccordion;
    @FXML private Accordion mentalAccordion;
    @FXML private Accordion situationalAccordion;
    @FXML private Accordion mathAccordion;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Expand the first pane of the microstakes section by default
        // to give users an immediate starting point
        if (microAccordion != null && !microAccordion.getPanes().isEmpty()) {
            microAccordion.setExpandedPane(microAccordion.getPanes().get(0));
        }
    }
}
