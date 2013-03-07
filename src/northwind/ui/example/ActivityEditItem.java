package northwind.ui.example;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import reactive.ui.*;
import tee.binding.*;
import tee.binding.it.*;
import tee.binding.task.*;
import android.os.*;
import android.app.*;
import android.content.*;
import android.graphics.*;

public class ActivityEditItem extends Activity {
	Layoutless layoutless;
	Note product = new Note();
	Note order = new Note();
	Note orderDate = new Note();
	Note orderEmplyee = new Note();
	Note orderCustomer = new Note();
	Note orderProduct = new Note();
	Note stockPrice = new Note();
	Numeric orderPrice = new Numeric();
	Numeric orderDiscount = new Numeric();
	Numeric orderQuantity = new Numeric();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		layoutless = new Layoutless(this);
		setContentView(layoutless);
		Bundle bundle = getIntent().getExtras();
		String sproduct = bundle.getString("product");
		String sorder = bundle.getString("order");
		if (sorder != null) {
			//formTitle.value("Order " + s);
			this.setTitle("Northwind UI Example - Order item");
			order.value(sorder);
			product.value(sproduct);
			scatterOrder(product.value(), order.value());
		}
		else {
			this.setTitle("Northwind UI Example - New order item");
		}
		compose();
	}
	void scatterOrder(String p, String o) {
		String sql = "select"//
				+ "		details.OrderID,details.ProductID,details.UnitPrice as OrderUnitPrice,details.Quantity,details.Discount"//
				+ "		,Products.ProductName,Products.QuantityPerUnit,Products.UnitPrice as ProductUnitPrice"//
				+ "		,Products.CategoryID,Categories.CategoryName"//
				+ "		,Products.SupplierID,Suppliers.CompanyName,Suppliers.City,Suppliers.Country"//
				+ " 	,Orders.OrderDate"//
				+ " 	,Employees.FirstName,Employees.LastName"//
				+ " 	,Customers.CompanyName as Customer"//
				+ "	from [Order Details] details"//
				+ "		join Products on Products.ProductID=details.ProductID"//
				+ "		join Suppliers on Suppliers.SupplierID=Products.SupplierID"//
				+ "		join Categories on Categories.CategoryID=Products.CategoryID"//
				+ " 	join Orders on Orders.OrderID=details.OrderID"//
				+ " 	join Employees on Orders.EmployeeID=Employees.EmployeeID"//
				+ " 	join Customers on Orders.CustomerID=Customers.CustomerID"//
				+ "	where details.OrderID=" + o + " and details.ProductID=" + p;
		//System.out.println(sql);
		Bough data = Auxiliary.fromCursor(Tools.db(this).rawQuery(sql, null), true);
		Bough row = data.children.get(0);
		orderDate.value(Tools.formatDate(Auxiliary.date(row.child("OrderDate").value.property.value())));
		orderEmplyee.value(row.child("FirstName").value.property.value() + " " + row.child("LastName").value.property.value());
		orderCustomer.value(row.child("Customer").value.property.value());
		orderProduct.value(row.child("ProductName").value.property.value() + ", " + row.child("CategoryName").value.property.value());
		stockPrice.value(row.child("ProductUnitPrice").value.property.value() + " x " + row.child("QuantityPerUnit").value.property.value());
		orderPrice.value(Numeric.string2double(row.child("OrderUnitPrice").value.property.value()));
		orderDiscount.value(Numeric.string2double(row.child("Discount").value.property.value()));
		orderQuantity.value(Numeric.string2double(row.child("Quantity").value.property.value()));
		//quantityPerUnit.value(row.child("QuantityPerUnit").value.property.value());
	}
	void compose() {
		layoutless//
		.innerHeight.is(10 * 0.8 * Auxiliary.tapSize)//
		.innerWidth.is(9 * Auxiliary.tapSize)//
		;
		layoutless.child(new Decor(this)//
				.background.is(Auxiliary.colorBackground)//
						.sketch(new SketchPlate().tint(new TintBitmapTile()//
								.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes3))//
								)//
						.width.is(layoutless.width().property)//
						.height.is(layoutless.height().property)//
						)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.labelText.is("Item")//
				.labelSize.is(0.2 * Auxiliary.screenWidth(this))//
				.labelColor.is(0xccffffff)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes4)).top().is(layoutless.height().property.minus(250)).width().is(400)//
						.height().is(250)//
				);
		layoutless.field(this, 0, "Order ID", new Decor(this).labelText.is(order).labelAlignLeftCenter().labelStyleMediumNormal());
		layoutless.field(this, 1, "Order date", new Decor(this).labelText.is(orderDate).labelAlignLeftCenter().labelStyleMediumNormal());
		layoutless.field(this, 2, "Employee", new Decor(this).labelText.is(orderEmplyee).labelAlignLeftCenter().labelStyleMediumNormal());
		layoutless.field(this, 3, "Customer", new Decor(this).labelText.is(orderCustomer).labelAlignLeftCenter().labelStyleMediumNormal());
		layoutless.field(this, 4, "Product", new KnobText(this).text.is(orderProduct).afterTap.is(new Task() {
			@Override
			public void doTask() {
				promptProduct();
			}
		}), 7 * Auxiliary.tapSize);
		layoutless.field(this, 5, "Stock price", new Decor(this).labelText.is(stockPrice).labelAlignLeftCenter().labelStyleMediumNormal());
		layoutless.field(this, 6, "Price", new RedactNumber(this).number.is(orderPrice), 2 * Auxiliary.tapSize);
		layoutless.field(this, 7, "Descount", new RedactNumber(this).number.is(orderDiscount), 2 * Auxiliary.tapSize);
		layoutless.field(this, 8, "Quantity", new RedactNumber(this).number.is(orderQuantity), 2 * Auxiliary.tapSize);
		//layoutless.field(this, 9, "Quantity per unit", new Decor(this).labelText.is(quantityPerUnit.value()).labelAlignLeftCenter().labelStyleMediumNormal());
	}
	void promptProduct() {
		//System.out.println("promptProduct");
		Intent intent = new Intent();
		intent.setClass(this, ActivityPromptProduct.class);
		//intent.putExtra("id", orderID);
		this.startActivityForResult(intent, 0);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String productID = intent.getStringExtra("productID");
				//System.out.println("promptProduct "+productID);
				String sql = "select"//
						+ "		Products.ProductName,Products.QuantityPerUnit,Products.UnitPrice as ProductUnitPrice"//
						+ "		,Products.CategoryID,Categories.CategoryName"//
						+ "	from Products"//
						+ "		join Categories on Categories.CategoryID=Products.CategoryID"//
						+ "	where Products.ProductID=" + productID;
				//System.out.println(sql);
				Bough data = Auxiliary.fromCursor(Tools.db(this).rawQuery(sql, null), true);
				Bough row = data.children.get(0);
				orderProduct.value(row.child("ProductName").value.property.value() + ", " + row.child("CategoryName").value.property.value());
				stockPrice.value(row.child("ProductUnitPrice").value.property.value() + " x " + row.child("QuantityPerUnit").value.property.value());
			}
		}
	}
}
