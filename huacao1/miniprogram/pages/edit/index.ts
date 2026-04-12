import type { PlantCategory, PlantFormData, PublishStatus } from '../../types/api'
import { COMMON_UNITS, MAX_UPLOAD_IMAGE_COUNT } from '../../utils/config'
import { promptMemberLogin } from '../../utils/login'
import { buildPlantPayload, createEmptyPlantForm, detailToPlantForm, ensureCoverImage, normalizePriceInput, validatePlantForm } from '../../utils/plant-form'
import { getPlantDetail, listPlantCategories, updatePlantItem, uploadPlantImages } from '../../utils/plant-service'

Page({
  data: {
    itemId: '',
    loading: true,
    uploading: false,
    submitting: false,
    categories: [] as PlantCategory[],
    unitOptions: COMMON_UNITS,
    form: createEmptyPlantForm() as PlantFormData,
  },

  onLoad(options: any) {
    const itemId = String(options.id || '')
    if (!itemId) {
      this.showToast('缺少内容编号')
      return
    }
    this.setData({
      itemId,
    })
    void this.bootstrap()
  },

  async bootstrap() {
    this.setData({
      loading: true,
    })
    try {
      await getApp<IAppOption>().ensureMemberSession()
    } catch (error) {
      await promptMemberLogin({
        message: (error as Error).message,
        replace: true,
      })
      this.setData({
        loading: false,
      })
      return
    }

    try {
      const [categories, detail] = await Promise.all([
        listPlantCategories(),
        getPlantDetail(this.data.itemId),
      ])
      this.setData({
        categories,
        form: detailToPlantForm(detail),
      })
    } catch (error) {
      this.showToast((error as Error).message)
    } finally {
      this.setData({
        loading: false,
      })
    }
  },

  onFieldInput(event: any) {
    const field = String(event.currentTarget.dataset.field || '')
    let value = event.detail.value
    if (field === 'suggestedRetailPrice') {
      value = normalizePriceInput(value)
    }
    this.setData({
      [`form.${field}`]: value,
    })
  },

  onSelectCategory(event: any) {
    this.setData({
      'form.categoryId': String(event.currentTarget.dataset.id || ''),
    })
  },

  onSelectUnit(event: any) {
    this.setData({
      'form.unit': String(event.currentTarget.dataset.unit || ''),
    })
  },

  async chooseImages() {
    const remainCount = MAX_UPLOAD_IMAGE_COUNT - this.data.form.images.length
    if (remainCount <= 0) {
      this.showToast(`最多上传 ${MAX_UPLOAD_IMAGE_COUNT} 张图片`)
      return
    }

    wx.chooseImage({
      count: remainCount,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (result) => {
        if (!result.tempFilePaths.length) {
          return
        }
        this.setData({
          uploading: true,
        })
        try {
          const uploaded = await uploadPlantImages(result.tempFilePaths)
          const nextImages = this.data.form.images.concat(uploaded.map((item) => ({
            url: item.url,
            size: item.size,
            fileName: item.fileName,
          })))
          const nextCoverImage = ensureCoverImage(nextImages.map((item) => item.url), this.data.form.coverImageUrl)
          this.setData({
            'form.images': nextImages,
            'form.coverImageUrl': nextCoverImage,
          })
        } catch (error) {
          this.showToast((error as Error).message)
        } finally {
          this.setData({
            uploading: false,
          })
        }
      },
    })
  },

  previewImage(event: any) {
    const current = String(event.currentTarget.dataset.url || '')
    if (!current) {
      return
    }
    wx.previewImage({
      current,
      urls: this.data.form.images.map((item) => item.url),
    })
  },

  removeImage(event: any) {
    const url = String(event.currentTarget.dataset.url || '')
    const nextImages = this.data.form.images.filter((item) => item.url !== url)
    this.setData({
      'form.images': nextImages,
      'form.coverImageUrl': ensureCoverImage(nextImages.map((item) => item.url), this.data.form.coverImageUrl),
    })
  },

  selectCover(event: any) {
    const url = String(event.currentTarget.dataset.url || '')
    if (!url) {
      return
    }
    this.setData({
      'form.coverImageUrl': url,
    })
  },

  async saveDraft() {
    await this.submit(0)
  },

  async saveUpdate() {
    const publishStatus = this.data.form.publishStatus === 2 ? 2 : 1
    await this.submit(publishStatus)
  },

  async submit(publishStatus: PublishStatus) {
    const validationMessage = validatePlantForm(this.data.form)
    if (validationMessage) {
      this.showToast(validationMessage)
      return
    }

    this.setData({
      submitting: true,
    })
    try {
      const detail = await updatePlantItem(this.data.itemId, buildPlantPayload(this.data.form, publishStatus))
      wx.showToast({
        title: '保存成功',
        icon: 'success',
      })
      if (publishStatus === 0) {
        wx.redirectTo({
          url: '/pages/mine-publish/index',
        })
        return
      }
      wx.redirectTo({
        url: `/pages/detail/index?id=${detail.id}`,
      })
    } catch (error) {
      this.showToast((error as Error).message)
    } finally {
      this.setData({
        submitting: false,
      })
    }
  },

  showToast(title: string) {
    wx.showToast({
      title: title || '请稍后重试',
      icon: 'none',
    })
  },
})
