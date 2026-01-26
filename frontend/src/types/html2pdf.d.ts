declare module 'html2pdf.js' {
  interface Html2PdfOptions {
    margin?: number | number[]
    filename?: string
    image?: Record<string, any>
    html2canvas?: Record<string, any>
    jsPDF?: Record<string, any>
    pagebreak?: Record<string, any>
    [key: string]: any
  }

  interface Html2PdfInstance {
    set(opt: Html2PdfOptions): Html2PdfInstance
    from(element: HTMLElement | string): Html2PdfInstance
    save(): Promise<void>
    outputPdf(type?: string): Promise<any>
    output(type?: string, options?: any): Promise<any>
  }

  function html2pdf(): Html2PdfInstance
  function html2pdf(element: HTMLElement, opt?: Html2PdfOptions): Html2PdfInstance

  export default html2pdf
}

