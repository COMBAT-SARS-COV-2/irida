import {EVENTS} from './../../constants';
const templateUrl = 'metadata.button.tmpl';
const asideTemplateUrl = 'metadata.aside.tmpl';

const FIELDS = window.headersList.map((header, index) => {
  return ({text: header, index, selected: true});
});

// Need to modify the fields if the table reorders the headers
// Listen for moving columns
document.body.addEventListener(EVENTS.TABLE.colReorder, e => {
  const {to, from} = e.detail;
  const temp = FIELDS[from];
  FIELDS[from] = FIELDS[to];
  FIELDS[from].index = from;
  FIELDS[to] = temp;
  FIELDS[to].index = to;
});

export const MetadataComponent = {
  templateUrl,
  controller: class MetadataComponent {
    constructor($aside) {
      this.$aside = $aside;
    }

    showMetadataTemplator() {
      this.$aside.open({
        templateUrl: asideTemplateUrl,
        openedClass: 'metadata-open',
        controllerAs: '$ctrl',
        controller() {
          this.fields = Object.assign(FIELDS);

          this.toggleField = field => {
            // Broadcast change column visibility
            const event = new CustomEvent(
              EVENTS.TABLE.columnVisibility,
              {
                detail: {
                  column: Number(field.index)
                },
                bubbles: true
              });
            document.body.querySelector('.modal-content')
              .dispatchEvent(event);
          };
        },
        placement: 'left',
        size: 'sm'
      });
    }
  }
};
