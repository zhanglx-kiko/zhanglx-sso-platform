Component({
  properties: {
    item: {
      type: Object,
      value: {},
    },
    showStatus: {
      type: Boolean,
      value: false,
    },
    showPublisher: {
      type: Boolean,
      value: true,
    },
  },
  methods: {
    handleTap() {
      this.triggerEvent('tapcard', this.properties.item)
    },
  },
})
