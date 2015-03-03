# Vertical Annotation Editor, a plugin for GATE

Vertical Annotation Editor is a plugin for GATE (https://gate.ac.uk/).
It provides a DocumentViewer plugin that allows editing annotations in a table.

This viewer simplifies manual coding tasks where the value of specific feature has
to be set for many annotations. Examples for such tasks are
lemmatisation and part-of-speech tagging where the gate annotations
mark the tokens and the lemma and the part-of-speech tags are encoded
as feature-value pairs for each annotation. 
The layout of the table is driven by Annotation Schemas (see
section 5.4.1 of the GATE User Guide at https://gate.ac.uk/sale/tao). Thus
it can be easily adapted to different annotation tasks.

The plugin was created by Fabian Barteld between 2013-2015 in the context of the DFG-funded project 
"Entwicklung der satzinternen Großschreibung im Deutschen. Eine
korpuslinguistische Studie zum Zusammenspiel kognitiv-semantischer und
syntaktischer Faktoren" (SIGS) headed by 
Renata Szczepaniak (Hamburg) and Klaus-Michael Köpcke (Münster).

## Installation

To use the plugin download the binary version of the current release.
The extracted folder can be loaded in GATE as a plugin.
Instructions can be found in section 3.5 of the GATE User Guide
(https://gate.ac.uk/sale/tao).

## Usage

After the plugin is loaded successfully the button "Vertical Annotation
Editor" appears on top of the main window in the document editor. By
clicking on this button the text view is replaced by the Vertical
Annotation Editor.

At the top of this view two drop-down lists allow to choose an
annotation set and an annotation type.
All corresponding annotations are shown in the table below.

Between the table and the two drop-down lists the filter panel can be found.
It allows filtering the shown annotations with feature-value pairs.
With "Add Feature/Value" constraints for the filter can be added.
When the check-box "Activate filters" is active the table only shows
annotations that contain feature-value pairs which match the text in
the text-boxes.

Unless an Annotation Schema for the current annotation type is loaded
the table is read-only and shows the annotations in a format similar
to the annotation list view, i.e. the start node, the end node, the id
and a list of all feature-value pairs.

If an Annotation Schema is loaded the table shows the text covered by
the annotation in the first column. This column is read-only.
The other columns show the attributes defined in the schema. They are
rendered depending on the type of the attributes, e.g., enumerations
are rendered as combo-boxes with auto-completion support, allowing
only values given in the schema (if the attribute is declared as
optional an empty value is allowed as well).
Values that are not allowed by the schema but that exist in the data
are rendered with a red border. 

## Known issues

Since the plugin does not check whether the types of the feature values
correspond to the types given by the Annotation Schema, 
an error might occur when the types of the feature values are not valid.
The only seen instance of this error occurs with attributes of the
type boolean. Showing the Vertical Annotation Editor fails when 
the corresponding feature of the annotations 
contains any other value than "true" or "false".

## License

Vertical Annotation Editor is free software: 
you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Vertical Annotation Editor is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see http://www.gnu.org/licenses/.

Der Vertical Annotation Editor ist Freie Software: 
Sie können sie unter den Bedingungen der GNU General Public License, 
wie von der Free Software Foundation, Version 3 der Lizenz oder 
(nach Ihrer Wahl) jeder neueren veröffentlichten Version,
weiterverbreiten und/oder modifizieren.
Der Vertical Annotation Editor wird in der Hoffnung, 
dass er nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; 
sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT
oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
Siehe die GNU General Public License für weitere Details.

Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
Programm erhalten haben. Wenn nicht, siehe http://www.gnu.org/licenses/.
